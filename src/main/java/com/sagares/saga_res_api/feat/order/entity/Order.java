package com.sagares.saga_res_api.feat.order.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Order extends BaseEntity {

    // Giữ tên user để khớp mappedBy của User.orders.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User user;

    @Column(name = "order_code", nullable = false, length = 32)
    private String orderCode;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // Hibernate tự khởi tạo và tăng phiên bản khi cập nhật.
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "order",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Set<OrderItem> orderItems = new HashSet<>();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Đồng bộ hai phía của quan hệ.
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}