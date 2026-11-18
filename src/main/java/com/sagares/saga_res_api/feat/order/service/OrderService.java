package com.sagares.saga_res_api.feat.order.service;


import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.common.exception.BusinessException;
import com.sagares.saga_res_api.common.exception.ResourceNotFoundException;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderAction;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderDetailResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.order.entity.OrderItem;
import com.sagares.saga_res_api.feat.order.entity.OrderStatusHistory;
import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.order.repository.OrderRepository;
import com.sagares.saga_res_api.feat.order.repository.OrderStatusHistoryRepository;
import com.sagares.saga_res_api.feat.payment.dto.StaffOrderPayment;
import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.payment.entity.PaymentTransaction;
import com.sagares.saga_res_api.feat.payment.entity.TransactionStatus;
import com.sagares.saga_res_api.feat.payment.entity.TransactionType;
import com.sagares.saga_res_api.feat.payment.repository.PaymentTransactionRepository;

import com.sagares.saga_res_api.feat.user.entity.Role;
import com.sagares.saga_res_api.feat.user.entity.User;
import com.sagares.saga_res_api.feat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<StaffOrderResponse> getPendingOrders(String cursor, int size) {
        return getStaffOrders(List.of(Status.PENDING), cursor, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<StaffOrderResponse> getProcessingOrders(String cursor, int size) {
        return getStaffOrders(
                List.of(Status.ACCEPTED, Status.PREPARING, Status.DELIVERING),
                cursor,
                size);
    }

    @Transactional(readOnly = true)
    public PageResponse<StaffOrderResponse> getFinishedOrders(String cursor, int size) {
        return getStaffOrders(
                List.of(Status.DENIED, Status.SUCCESSFUL, Status.REFUND),
                cursor,
                size);
    }

    @Transactional(readOnly = true)
    public StaffOrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        List<StaffOrderDetailResponse.Item> items = order.getOrderItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .map(item -> new StaffOrderDetailResponse.Item(
                        item.getId(),
                        item.getMenu().getName(),
                        item.getMenu().getImageUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubTotal()))
                .toList();

        StaffOrderDetailResponse.Transaction payment = findDetailTransaction(orderId, TransactionType.PAYMENT);
        StaffOrderDetailResponse.Transaction refund = findDetailTransaction(orderId, TransactionType.REFUND);

        return new StaffOrderDetailResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getReason(),
                new StaffOrderDetailResponse.Delivery(
                        order.getUser().getFullName(),
                        null, // Order chưa lưu số điện thoại người nhận.
                        order.getDeliveryAddress(),
                        order.getNote()),
                items,
                payment,
                refund,
                allowedActions(order.getStatus(), payment, refund));
    }

    //Duyệt đơn VnPay PENDING -> APPROAED (sai chính tả rr)
    @Transactional
    public void approveOrder(Long orderId, String staffEmail) {

        User staff = userRepository.findByEmailAndDeletedFalse(staffEmail)
                .filter(user -> user.getRole() == Role.STAFF)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.FORBIDDEN,
                        "STAFF_REQUIRED",
                        "Tài khoản staff không hợp lệ"));

        //lấy đơn
        Order order = orderRepository.findByIdAndDeletedFalse(orderId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        //lấy trạng thái hiện tại và kiểm tra trạng thái
        Status fromStatus = order.getStatus();
        if (fromStatus != Status.PENDING) {
            throw new BusinessException(
                    "ORDER_NOT_PENDING", "Đơn hàng không còn chờ duyệt");
        }

        //lấy phương thức thanh toán
        PaymentTransaction payment = paymentTransactionRepository.findDetailTransactions(
                        orderId,
                        TransactionType.PAYMENT,
                        TransactionStatus.SUCCESS,
                        PageRequest.ofSize(1))
                .stream()
                .findFirst()
                .orElse(null);

        if (payment == null
                || payment.getMethod() != PaymentMethod.VNPAY
                || payment.getStatus() != TransactionStatus.SUCCESS) {
            throw new BusinessException(
                    "ORDER_PAYMENT_NOT_CONFIRMED",
                    "Chỉ duyệt đơn VNPAY đã xác nhận thu tiền");
        }
        //Set trạng thái của đơn đặt món sang đã duyệt
        order.setStatus(Status.ACCEPTED);

        //orderRepository.flush() yêu cầu Hibernate gửi các thay đổi đang chờ xuống database ngay lúc gọi.
        //Trong đoạn duyệt đơn, mục đích là để câu UPDATE chạy bên trong khối try, nơi service có thể bắt lỗi xung đột và chuyển thành ORDER_CHANGED
        try {
            orderRepository.flush();
        } catch (OptimisticLockingFailureException ex) {
            throw new BusinessException(
                    "ORDER_CHANGED", "Đơn đã được thay đổi. Vui lòng tải lại chi tiết đơn.");
        }

        //Lưu lịch sử chuyển đổi trạng thái
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(order.getStatus())
                .changedBy(staff)
                .build());
    }

    private StaffOrderDetailResponse.Transaction findDetailTransaction(Long orderId, TransactionType type) {
        return paymentTransactionRepository.findDetailTransactions(
                        orderId, type, TransactionStatus.SUCCESS, PageRequest.ofSize(1))
                .stream()
                .findFirst()
                .map(StaffOrderDetailResponse.Transaction::from)
                .orElse(null);
    }

    private List<StaffOrderAction> allowedActions(
            Status status, StaffOrderDetailResponse.Transaction payment, StaffOrderDetailResponse.Transaction refund) {
        return switch (status) {
            case PENDING -> pendingActions(payment);
            case ACCEPTED -> List.of(StaffOrderAction.START_PREPARING, StaffOrderAction.REJECT_ORDER);
            case PREPARING -> List.of(StaffOrderAction.START_DELIVERY);
            case DELIVERING -> deliveringActions(payment);
            case DENIED, REFUND -> refund != null && refund.status() == TransactionStatus.PENDING
                    ? List.of(StaffOrderAction.REFRESH_REFUND_STATUS)
                    : List.of();
            case SUCCESSFUL -> List.of();
        };
    }

    private List<StaffOrderAction> pendingActions(StaffOrderDetailResponse.Transaction payment) {
        //trường hợp trạng thái thất bại chỉ cho từ chối
        if (payment == null || payment.status() == TransactionStatus.FAILED) {
            return List.of(StaffOrderAction.REJECT_ORDER);
        }
        //trường hợp đơn thanh toán cod cho phép bắt đầu chuẩn bị và tuwf chối đơn hàng
        if (payment.method() == PaymentMethod.COD) {
            return List.of(StaffOrderAction.START_PREPARING, StaffOrderAction.REJECT_ORDER);
        }
        //trường hợp thanh toán thành công (VNPAY) cho phép đồng ý order và từ chối order
        if (payment.status() == TransactionStatus.SUCCESS && payment.method() == PaymentMethod.VNPAY) {
            return List.of(StaffOrderAction.APPROVE_ORDER, StaffOrderAction.REJECT_ORDER);
        }

        return List.of(StaffOrderAction.RECONCILE_PAYMENT, StaffOrderAction.REJECT_ORDER);
    }

    private List<StaffOrderAction> deliveringActions(StaffOrderDetailResponse.Transaction payment) {
        if (payment != null && payment.status() == TransactionStatus.SUCCESS) {
            return List.of(StaffOrderAction.COMPLETE_DELIVERY);
        }
        if (payment != null && payment.method() == PaymentMethod.COD
                && payment.status() == TransactionStatus.PENDING) {
            return List.of(StaffOrderAction.CONFIRM_COD_COLLECTION);
        }
        return List.of();
    }

    private PageResponse<StaffOrderResponse> getStaffOrders(List<Status> statuses, String cursor, int size) {

        //Lấy +1 để kiểm tra limited
        PageRequest limit = PageRequest.ofSize(size + 1);
        List<Order> batch;

        if (cursor == null) {
            batch = orderRepository.findByStatusInAndDeletedFalseOrderByCreatedAtAscIdAsc(statuses, limit);
        } else {
            PageResponse.Cursor after;
            try {
                after = PageResponse.parseCursor(cursor);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cursor không hợp lệ", ex);
            }

            batch = orderRepository.findAfter(statuses, after.createdAt(), after.id(), limit);
        }

        PageResponse<Order> page = PageResponse.of(
                batch,
                size,
                order -> PageResponse.formatCursor(order.getCreatedAt(), order.getId()));

        if (page.content().isEmpty()) {
            return new PageResponse<>(List.of(), null, false);
        }

        List<Long> orderIds = page.content().stream().map(Order::getId).toList();
        Map<Long, StaffOrderPayment> paymentByOrderId = new HashMap<>();

        for (StaffOrderPayment payment : paymentTransactionRepository.findOrderPayments(
                orderIds, TransactionType.PAYMENT, TransactionStatus.SUCCESS)) {
            paymentByOrderId.putIfAbsent(payment.getOrderId(), payment);
        }

        List<StaffOrderResponse> items = page.content().stream()
                .map(order -> StaffOrderResponse.toDTO(order, paymentByOrderId.get(order.getId())))
                .toList();

        return new PageResponse<>(items, page.nextCursor(), page.hasNext());
    }

}
