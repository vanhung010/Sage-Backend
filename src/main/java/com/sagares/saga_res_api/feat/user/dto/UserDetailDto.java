package com.sagares.saga_res_api.feat.user.dto;


import com.sagares.saga_res_api.feat.user.Role;

public record UserDetailDto(
        Long id,
        String fullName,
        String email,
        Role role
) {
}