package com.devfat.mini_ecommerce.home.dto;

import java.math.BigDecimal;
import java.util.List;

public record FeaturedProductDto(
        Long id,
        String name,
        String categoryLabel,
        String categorySlug,
        List<String> badges,
        String spec,
        BigDecimal price,
        BigDecimal originalPrice,
        String unit,
        String imageUrl,
        BigDecimal rating,
        Integer reviewCount,
        String origin,
        String description,
        List<String> weightOptions
) {}
