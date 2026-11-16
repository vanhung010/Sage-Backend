package com.sagares.saga_res_api.feat.order.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;

import com.sagares.saga_res_api.feat.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_status_history", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // NULL ở sự kiện tạo đơn đầu tiên.
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private Status toStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
}