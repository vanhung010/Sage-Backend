package com.sagares.saga_res_api.feat.user.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_contact", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ShippingContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}