package com.sagares.saga_res_api.feat.order.service;


import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.common.exception.BusinessException;
import com.sagares.saga_res_api.common.exception.ResourceNotFoundException;
import com.sagares.saga_res_api.feat.notification.service.OrderNotificationService;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderAction;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderDetailResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.order.entity.OrderItem;
import com.sagares.saga_res_api.feat.order.entity.OrderStatusHistory;
import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.order.event.OrderStatusChangedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderNotificationService orderNotificationService;

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

        StaffOrderDetailResponse.TransactionOrder payment = findDetailTransaction(orderId, TransactionType.PAYMENT);
        StaffOrderDetailResponse.TransactionOrder refund = findDetailTransaction(orderId, TransactionType.REFUND);

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
        User staff =  requireStaff(staffEmail);

        //lấy đơn
        Order order = requireOrder(orderId);

        if (order.getStatus() != Status.PENDING) {
            throw new BusinessException(
                    "ORDER_NOT_PENDING",
                    "Đơn hàng không còn chờ duyệt");
        }

        var payment = findDetailTransaction(orderId, TransactionType.PAYMENT);

        if (!isPaidVnpay(payment)) {
            throw new BusinessException(
                    "ORDER_PAYMENT_NOT_CONFIRMED",
                    "Chỉ duyệt đơn VNPAY đã xác nhận thu tiền");
        }

        changeStatus(order, Status.ACCEPTED, staff);
    }

    //Bắt đầu chuẩn bị món ăn ACCCEPT -> PREPAIR (VnPay) và PENDING -> PREPAIR
    @Transactional
    public void startPreparing(Long orderId, Status expectedStatus, String staffEmail) {
        //Lấy và kiểm tra phải staff không
        User staff = requireStaff(staffEmail);
        //Lấy order
        Order order = requireOrder(orderId);

        if (order.getStatus() != expectedStatus) {
            throw new BusinessException("ORDER_CHANGED",
                    "Đơn không còn ở trạng thái " + expectedStatus
                            + ". Vui lòng tải lại chi tiết đơn.");
        }

        var payment = findDetailTransaction(orderId, TransactionType.PAYMENT);

        if (!canStartPreparing(expectedStatus, payment)) {
            throw new BusinessException("ORDER_PAYMENT_NOT_ELIGIBLE",
                    "Giao dịch thanh toán của đơn không đủ điều kiện để bắt đầu chuẩn bị.");
        }

        changeStatus(order, Status.PREPARING, staff);
    }


    private User requireStaff(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .filter(user -> user.getRole() == Role.STAFF)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.FORBIDDEN,
                        "STAFF_REQUIRED",
                        "Tài khoản staff không hợp lệ"));
    }

    private Order requireOrder(Long orderId) {
        return orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng"));
    }



    private boolean canStartPreparing(Status status,
                                      StaffOrderDetailResponse.TransactionOrder payment) {
        return switch (status) {
            case ACCEPTED -> isPaidVnpay(payment);
            case PENDING -> payment != null
                    && payment.method() == PaymentMethod.COD
                    && (payment.status() == TransactionStatus.PENDING
                    || payment.status() == TransactionStatus.SUCCESS);
            default -> false;
        };
    }
    //Kiểm tra TransactionOrder có phải VnPay và đã thanh toán không
    private boolean isPaidVnpay(StaffOrderDetailResponse.TransactionOrder payment) {
        return payment != null
                && payment.method() == PaymentMethod.VNPAY
                && payment.status() == TransactionStatus.SUCCESS;
    }

    private void changeStatus(Order order, Status toStatus, User staff) {
        //Trạng thái đầu
        Status fromStatus = order.getStatus();
        //Đổi trạng thái
        order.setStatus(toStatus);
        //Ép lưu trạng thaái, nếu có lỗi thì bắn ra ở đây.
        try {
            orderRepository.flush();
        } catch (OptimisticLockingFailureException ex) {
            throw new BusinessException("ORDER_CHANGED",
                    "Đơn đã được thay đổi. Vui lòng tải lại chi tiết đơn.");
        }
        //Lưu lịch sử thay đổi trạng thái
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(staff)
                .build());
        //Vừa lưu vừa bắn thoong báo mới cho user biết
        orderNotificationService.recordStatusChange(order);
        //Bắn thông báo thay đổi trạng thái cho nhan viên cập nhật giao diện
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                order.getOrderCode(),
                order.getUser().getEmail(),
                fromStatus,
                toStatus,
                null));
    }



    private StaffOrderDetailResponse.TransactionOrder findDetailTransaction(Long orderId, TransactionType type) {
        return paymentTransactionRepository.findDetailTransactions(orderId, type, TransactionStatus.SUCCESS, PageRequest.ofSize(1))
                .stream()
                .findFirst()
                .map(StaffOrderDetailResponse.TransactionOrder::from)
                .orElse(null);
    }

    private List<StaffOrderAction> allowedActions(Status status, StaffOrderDetailResponse.TransactionOrder payment, StaffOrderDetailResponse.TransactionOrder refund) {
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

    private List<StaffOrderAction> pendingActions(StaffOrderDetailResponse.TransactionOrder payment) {
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

    private List<StaffOrderAction> deliveringActions(StaffOrderDetailResponse.TransactionOrder payment) {
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
