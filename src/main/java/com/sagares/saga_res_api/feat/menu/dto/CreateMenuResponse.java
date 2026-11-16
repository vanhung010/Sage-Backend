package com.sagares.saga_res_api.feat.menu.dto;

import com.sagares.saga_res_api.feat.menu.entity.Menu;
import lombok.Builder;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record CreateMenuResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        boolean isAvailable,
        BigDecimal price
) {
    public static CreateMenuResponse toDTO(Menu menu){
        return new CreateMenuResponse(menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.isAvailable(),
                menu.getPrice());
    }
}
