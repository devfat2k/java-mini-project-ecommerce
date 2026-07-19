package com.devfat.mini_ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDto(
        @NotNull Long productId,
        @Min(1) Integer quantity
) {}
