package com.devfat.mini_ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateRolePermissionsRequestDto(
        @NotNull
        Set<Long> permissionIds
) {
}
