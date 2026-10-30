package com.sagares.saga_res_api.feat.menu.dto;

import lombok.Builder;

import java.math.BigInteger;

@Builder(toBuilder = true)
public record MenuDto(
        BigInteger id,
        String name,
        String description,
        String imageUrl,
        boolean isAvailable
) {
}
