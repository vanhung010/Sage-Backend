package com.sagares.saga_res_api.feat.menu.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateMenuRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String description,
        @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 0)
        BigDecimal price,
        boolean isAvailable,
        List<@NotNull @Positive Long> categoryIds
) {}