package com.sagares.saga_res_api.feat.cart.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}