package com.devfat.mini_ecommerce.home.dto;

import java.time.LocalDateTime;

public record FeaturedReviewDto(
        Long id,
        String customerName,
        String customerLocation,
        String avatarInitials,
        Integer rating,
        String productName,
        String comment,
        LocalDateTime createdAt
) {
}
