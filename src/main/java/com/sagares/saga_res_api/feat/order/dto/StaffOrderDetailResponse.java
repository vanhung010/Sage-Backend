package com.sagares.saga_res_api.feat.order.dto;

import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.payment.entity.PaymentTransaction;
import com.sagares.saga_res_api.feat.payment.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StaffOrderDetailResponse(
        Long id,
        String orderCode,
        Status status,
        BigDecimal totalAmount,
        String reason,
        Delivery delivery,
        List<Item> items,
        TransactionOrder payment,
        TransactionOrder refund,
        List<StaffOrderAction> allowedActions
) {
    public record Delivery(
            String customerName,
            String recipientPhone,
            String address,
            String note
    ) {}

    public record Item(
            Long id,
            String name,
            String imageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subTotal
    ) {}

    public record TransactionOrder(
            PaymentMethod method,
            TransactionStatus status,
            BigDecimal amount,
            String reference,
            String providerTransactionId,
            LocalDateTime confirmedAt
    ) {
        public static TransactionOrder from(PaymentTransaction transaction) {
            return new TransactionOrder(
                    transaction.getMethod(),
                    transaction.getStatus(),
                    transaction.getAmount(),
                    transaction.getReference(),
                    transaction.getProviderTransactionId(),
                    transaction.getConfirmedAt());
        }
    }


}