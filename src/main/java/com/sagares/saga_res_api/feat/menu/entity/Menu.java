package com.sagares.saga_res_api.feat.menu.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menus", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Menu extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    // Danh mục được tạo riêng, rồi lấy từ repository để gắn vào món.
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "menu_categories",
            schema = "sage",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}