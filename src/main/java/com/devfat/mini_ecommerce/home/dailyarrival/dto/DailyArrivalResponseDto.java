package com.devfat.mini_ecommerce.home.dailyarrival.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyArrivalResponseDto(
        Long id,
        Long productId,
        LocalDate date,
        LocalDateTime arrivedAt,
        String badge,
        String title,
        String description,
        String weight,
        String origin,
        BigDecimal price,
        BigDecimal originalPrice,
        String imageUrl,
        String imageAlt,
        boolean isActive
) {}
