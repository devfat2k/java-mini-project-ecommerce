package com.devfat.mini_ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequestDto(
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String description,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal price,  // đúng kiểu ngay từ đầu
        @NotNull @Min(1) Integer stock,
        @NotNull Long categoryId,
        boolean isActive
) {}
