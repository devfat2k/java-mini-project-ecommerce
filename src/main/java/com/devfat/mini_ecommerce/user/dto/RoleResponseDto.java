package com.devfat.mini_ecommerce.user.dto;

import java.util.Set;

public record RoleResponseDto(
        Long id,
        String name,
        String description,
        Set<PermissionResponseDto> permissions
) {}
