package com.sagares.saga_res_api.feat.order.dto;

import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.payment.dto.StaffOrderPayment;
import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.payment.entity.TransactionStatus;
import com.sagares.saga_res_api.feat.payment.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StaffOrderResponse(
        Long id,
        String orderCode,
        String customerName,
        BigDecimal totalAmount,
        Status status,
        LocalDateTime createdAt,
        PaymentMethod paymentMethod,
        TransactionStatus paymentStatus
) {

    public static StaffOrderResponse toDTO(Order order, StaffOrderPayment payment) {
        return new StaffOrderResponse(
                order.getId(), order.getOrderCode(), order.getUser().getFullName(),
                order.getTotalAmount(), order.getStatus(), order.getCreatedAt(),
                payment == null ? null : payment.getMethod(),
                payment == null ? null : payment.getStatus());
    }
}
