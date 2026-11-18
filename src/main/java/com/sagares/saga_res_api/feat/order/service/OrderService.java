package com.sagares.saga_res_api.feat.order.service;


import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.common.exception.ResourceNotFoundException;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderAction;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderDetailResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.order.entity.OrderItem;
import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.order.repository.OrderRepository;
import com.sagares.saga_res_api.feat.payment.dto.StaffOrderPayment;
import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.payment.entity.TransactionStatus;
import com.sagares.saga_res_api.feat.payment.entity.TransactionType;
import com.sagares.saga_res_api.feat.payment.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;
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
            case PREPARING -> List.of(StaffOrderAction.START_DELIVERY, StaffOrderAction.CANCEL_ORDER);
            case DELIVERING -> deliveringActions(payment);
            case DENIED, REFUND -> refund != null && refund.status() == TransactionStatus.PENDING
                    ? List.of(StaffOrderAction.REFRESH_REFUND_STATUS)
                    : List.of();
            case SUCCESSFUL -> List.of();
        };
    }

    private List<StaffOrderAction> pendingActions(StaffOrderDetailResponse.Transaction payment) {
        if (payment == null || payment.status() == TransactionStatus.FAILED) {
            return List.of(StaffOrderAction.REJECT_ORDER);
        }
        if (payment.method() == PaymentMethod.COD) {
            return List.of(StaffOrderAction.START_PREPARING, StaffOrderAction.REJECT_ORDER);
        }
        if (payment.status() == TransactionStatus.SUCCESS) {
            return List.of(StaffOrderAction.APPROVE_ORDER, StaffOrderAction.REJECT_ORDER);
        }
        return List.of(StaffOrderAction.RECONCILE_PAYMENT, StaffOrderAction.REJECT_ORDER);
    }

    private List<StaffOrderAction> deliveringActions(StaffOrderDetailResponse.Transaction payment) {
        if (payment != null && payment.status() == TransactionStatus.SUCCESS) {
            return List.of(StaffOrderAction.COMPLETE_DELIVERY, StaffOrderAction.CANCEL_ORDER);
        }
        if (payment != null && payment.method() == PaymentMethod.COD
                && payment.status() == TransactionStatus.PENDING) {
            return List.of(StaffOrderAction.CONFIRM_COD_COLLECTION, StaffOrderAction.CANCEL_ORDER);
        }
        return List.of(StaffOrderAction.CANCEL_ORDER);
    }

    private PageResponse<StaffOrderResponse> getStaffOrders(List<Status> statuses, String cursor, int size) {

        //Lấy +1 để kiểm tra limited
        PageRequest limit = PageRequest.ofSize(size + 1);
        List<Order> batch;

        if (cursor == null) {
            batch = orderRepository.findByStatusInAndDeletedFalseOrderByCreatedAtAscIdAsc(
                    statuses, limit);
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
