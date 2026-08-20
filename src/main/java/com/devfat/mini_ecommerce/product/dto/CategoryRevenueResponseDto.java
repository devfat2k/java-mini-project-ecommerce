package com.devfat.mini_ecommerce.product.dto;

import java.math.BigDecimal;

public record CategoryRevenueResponseDto(
        String name,
        BigDecimal revenue
) {}
