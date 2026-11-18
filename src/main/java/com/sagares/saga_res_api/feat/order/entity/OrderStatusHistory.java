package com.sagares.saga_res_api.feat.order.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 20)
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private Status toStatus;

    // Để null khi trạng thái được hệ thống tự động cập nhật.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
}

