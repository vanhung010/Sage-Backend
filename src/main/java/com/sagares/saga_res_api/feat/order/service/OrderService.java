package com.sagares.saga_res_api.feat.order.service;


import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.order.repository.OrderRepository;
import com.sagares.saga_res_api.feat.payment.dto.StaffOrderPayment;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
