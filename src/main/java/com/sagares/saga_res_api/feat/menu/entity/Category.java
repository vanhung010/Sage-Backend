package com.sagares.saga_res_api.feat.menu.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Table(name = "categories")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Category extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "categories")
    private Set<Menu> menus = new HashSet<>();
}
