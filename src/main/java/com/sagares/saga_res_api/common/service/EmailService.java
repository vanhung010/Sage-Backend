package com.sagares.saga_res_api.common.service;

import com.sagares.saga_res_api.feat.order.entity.Status;
import com.sagares.saga_res_api.feat.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void send(OrderStatusChangedEvent event) {
        String statusText = switch (event.toStatus()) {
            case PENDING -> "đang chờ duyệt";
            case ACCEPTED -> "đã được duyệt";
            case DENIED -> "đã bị từ chối";
            case PREPARING -> "đang được chuẩn bị";
            case DELIVERING -> "đang được giao";
            case SUCCESSFUL -> "đã giao thành công";
            case REFUND -> "đã được hoàn tiền";
        };

        String subject = "Đơn %s %s".formatted(event.orderCode(), statusText);
        String body = "Xin chào,\n\nĐơn %s của bạn %s."
                .formatted(event.orderCode(), statusText);

        if (event.toStatus() == Status.DENIED
                && event.reason() != null
                && !event.reason().isBlank()) {
            body += "\n\nLý do từ chối\n" + event.reason();
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(event.customerEmail());
        message.setSubject(subject);
        message.setText(body + "\n\nCảm ơn bạn đã đặt món tại Sage.");

        mailSender.send(message);
    }

}
