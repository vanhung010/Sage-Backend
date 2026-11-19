package com.sagares.saga_res_api.feat.order.listener;

import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderStatusWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        var update = new OrderStatusUpdate(
                event.orderId().toString(),
                event.toStatus());

        messagingTemplate.convertAndSend("/topic/staff/orders", update);

        messagingTemplate.convertAndSendToUser(
                event.customerEmail(), "/queue/order-status", update);
    }

    public record OrderStatusUpdate(String orderId, Status status) {}
}