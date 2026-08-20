package com.devfat.mini_ecommerce.home.dailyarrival.dto;

import java.time.LocalDate;

public record CreateDailyArrivalRequestDto(
        Long productId,
        LocalDate date,
        String badge,
        String title,
        String description,
        String weight,
        String origin,
        Boolean isActive
) {}
