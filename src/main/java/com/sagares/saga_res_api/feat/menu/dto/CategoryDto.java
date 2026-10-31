package com.sagares.saga_res_api.feat.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigInteger;


@Builder(toBuilder = true)
public record CategoryDto(
        Long id,

        @NotBlank(message = "Name is not blank")
        @NotNull(message = "Name is null")
        String name,

        @NotNull(message = "Description is null")
        String description
) {
}
