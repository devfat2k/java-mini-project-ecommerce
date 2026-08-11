package com.devfat.mini_ecommerce.category.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequestDto(
        @NotNull @NotBlank String name
) {}
