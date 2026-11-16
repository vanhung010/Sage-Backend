package com.sagares.saga_res_api.feat.menu.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @ManyToMany(mappedBy = "categories")
    private Set<Menu> menus = new HashSet<>();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}