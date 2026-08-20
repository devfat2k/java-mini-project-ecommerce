package com.devfat.mini_ecommerce.home.dto;

import java.math.BigDecimal;

public record ComboSetDto(
        Long id,
        String tag,
        String title,
        String description,
        BigDecimal price,
        String unit,
        String ctaText,
        String href,
        String imageUrl,
        String theme,         // "light" | "dark"
        boolean isBreakout,
        String category,      // "lunch" | "party" | "family"
        Integer sortOrder,
        boolean isActive
) {
}
