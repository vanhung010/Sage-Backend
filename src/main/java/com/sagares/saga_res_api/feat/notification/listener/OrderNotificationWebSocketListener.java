package com.sagares.saga_res_api.feat.notification.listener;

import com.sagares.saga_res_api.feat.notification.event.OrderNotificationCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationWebSocketListener {

    public static final String QUEUE = "/queue/order-status";

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(OrderNotificationCreated event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    event.recipientId().toString(), QUEUE, event.payload());
        } catch (MessagingException ex) {
            log.warn("Không gửi được WebSocket cho thông báo {}", event.payload().id(), ex);
        }
    }
}