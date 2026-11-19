package com.sagares.saga_res_api.feat.order.event;



import com.sagares.saga_res_api.feat.order.entity.Status;

public record OrderStatusChangedEvent(
        Long orderId,
        String orderCode,
        String customerEmail,
        Status fromStatus,
        Status toStatus,
        String reason
) {}
