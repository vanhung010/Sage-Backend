package com.sagares.saga_res_api.feat.notification.event;

import com.sagares.saga_res_api.feat.order.entity.Status;

import java.time.LocalDateTime;

public record OrderNotificationCreated(Long recipientId, Payload payload) {

    public record Payload(
            String id,
            String orderId,
            String type,
            String title,
            String body,
            Status orderStatus,
            LocalDateTime createdAt) {
    }
}