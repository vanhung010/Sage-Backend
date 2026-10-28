package com.sagares.saga_res_api.feat.menu.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Table(name = "menus")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Menu extends BaseEntity {
    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "isAvailable", nullable = false)
    private boolean isAvailable;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "menu_categories",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

}
