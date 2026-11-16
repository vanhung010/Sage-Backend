package com.sagares.saga_res_api.feat.user.entity;
import com.sagares.saga_res_api.common.base.BaseEntity;
import com.sagares.saga_res_api.feat.order.entity.Order;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    // Service phải mã hóa trước khi lưu.
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<ShippingContact> shippingContacts = new HashSet<>();

    // Quản lý đơn độc lập để tránh xóa tài khoản làm mất lịch sử đơn.
    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new HashSet<>();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}