package com.sagares.saga_res_api.feat.user.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "shipping_contact")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingContact extends BaseEntity {
    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_default")
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
