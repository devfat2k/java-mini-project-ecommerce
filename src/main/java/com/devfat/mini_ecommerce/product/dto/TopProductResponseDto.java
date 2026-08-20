package com.devfat.mini_ecommerce.product.dto;

import java.math.BigDecimal;

public record TopProductResponseDto(
        String name,
        BigDecimal price,
        Integer mostBuy
) {}
