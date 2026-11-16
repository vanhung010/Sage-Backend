package com.sagares.saga_res_api.feat.order.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 0)
    private BigDecimal unitPrice;

    @Column(name = "sub_total", nullable = false, precision = 12, scale = 0)
    private BigDecimal subTotal;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}