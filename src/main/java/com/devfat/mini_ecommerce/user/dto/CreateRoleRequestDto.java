package com.devfat.mini_ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequestDto(
        @NotBlank(message = "Role name is not blank")
        String name,

        @NotBlank(message = "Description is not blank")
        String description
) {}
