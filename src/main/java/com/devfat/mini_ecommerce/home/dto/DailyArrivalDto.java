package com.devfat.mini_ecommerce.home.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DailyArrivalDto(
        Long id,
        Long productId,
        LocalDateTime arrivedAt,
        String badge,
        String title,
        String description,
        String weight,
        String origin,
        BigDecimal price,
        BigDecimal originalPrice,
        String imageUrl,
        String imageAlt
) {
}
