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

        //Lấy size +1 để kiểm tra còn đơn để tải tiếp không
        PageRequest limit = PageRequest.ofSize(size + 1);

        List<Order> batch;

        if (cursor == null) {
            //Lượt tải đầu tiên
            batch = orderRepository.findByStatusAndDeletedFalseOrderByCreatedAtAscIdAsc(Status.PENDING, limit);
        } else {
            PageResponse.Cursor after;
            try {
                //Phân tích cursor
                after = PageResponse.parseCursor(cursor);
            } catch (IllegalArgumentException ex) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cursor không hợp lệ", ex);
            }

            batch = orderRepository.findAfter(Status.PENDING, after.createdAt(), after.id(), limit);
        }

        //Tạo đối tượng page response theo kiểu order
        PageResponse<Order> page = PageResponse.of(batch, size, order -> PageResponse.formatCursor(order.getCreatedAt(), order.getId()));

        List<Order> orders = page.content();
        //Trường hợp rỗng
        if (orders.isEmpty()) return new PageResponse<>(List.of(), null, false);

        //Danh saách id của các đơn order
        List<Long> orderIds = orders.stream().map(Order::getId).toList();

        //Map lưu id, phương thức thanh toán cuả đơn
        Map<Long, StaffOrderPayment> paymentByOrderId = new HashMap<>();

        for (StaffOrderPayment payment : paymentTransactionRepository.findOrderPayments(orderIds, TransactionType.PAYMENT, TransactionStatus.SUCCESS)) {
            paymentByOrderId.putIfAbsent(payment.getOrderId(), payment);
        }

        List<StaffOrderResponse> items = orders.stream()
                .map(order -> StaffOrderResponse.toDTO(order, paymentByOrderId.get(order.getId())))
                .toList();
        return new PageResponse<>(items, page.nextCursor(), page.hasNext());
    }




}
