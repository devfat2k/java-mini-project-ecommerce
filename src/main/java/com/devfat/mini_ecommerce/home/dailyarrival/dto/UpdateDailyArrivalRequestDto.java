package com.devfat.mini_ecommerce.home.dailyarrival.dto;

public record UpdateDailyArrivalRequestDto(
        String badge,
        String title,
        String description,
        String weight,
        String origin,
        Boolean isActive
) {}
