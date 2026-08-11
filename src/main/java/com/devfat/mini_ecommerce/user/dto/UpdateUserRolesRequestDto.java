package com.devfat.mini_ecommerce.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateUserRolesRequestDto(
        @NotNull
        Set<Long> roleIds
) {}
