package com.sagares.saga_res_api.feat.notification.service;

import com.sagares.saga_res_api.feat.notification.entity.Notification;
import com.sagares.saga_res_api.feat.notification.event.OrderNotificationCreated;
import com.sagares.saga_res_api.feat.notification.repository.NotificationRepository;
import com.sagares.saga_res_api.feat.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordStatusChange(Order order) {
        String statusText = switch (order.getStatus()) {
            case PENDING -> "đang chờ duyệt";
            case ACCEPTED -> "đã được duyệt";
            case DENIED -> "đã bị từ chối";
            case PREPARING -> "đang được chuẩn bị";
            case DELIVERING -> "đang được giao";
            case SUCCESSFUL -> "đã giao thành công";
            case REFUND -> "đã được hoàn tiền";
        };

        Notification notification = notificationRepository.save(Notification.builder()
                .recipient(order.getUser())
                .order(order)
                .type("ORDER_STATUS_CHANGED")
                .title("Cập nhật trạng thái đơn hàng")
                .body("Đơn %s của bạn %s.".formatted(order.getOrderCode(), statusText))
                .build());

        var payload = new OrderNotificationCreated.Payload(
                notification.getId().toString(),
                order.getId().toString(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                order.getStatus(),
                notification.getCreatedAt());

        eventPublisher.publishEvent(new OrderNotificationCreated(order.getUser().getId(), payload));
    }
}