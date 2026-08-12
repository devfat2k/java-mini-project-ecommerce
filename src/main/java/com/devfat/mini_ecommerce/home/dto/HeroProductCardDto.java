package com.devfat.mini_ecommerce.home.dto;

import java.math.BigDecimal;

public record HeroProductCardDto(
        String imageUrl,
        String imageAlt,
        String comboBadge,
        String discountBadge,
        BigDecimal originalPrice,
        BigDecimal salePrice,
        String title,
        String subtitle
) {}
