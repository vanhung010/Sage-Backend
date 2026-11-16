package com.sagares.saga_res_api.feat.notification.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}