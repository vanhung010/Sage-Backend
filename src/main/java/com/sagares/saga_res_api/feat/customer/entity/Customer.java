package com.sagares.saga_res_api.feat.customer.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Table(name = "customers")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Customer extends BaseEntity {
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders = new HashSet<>();
}
