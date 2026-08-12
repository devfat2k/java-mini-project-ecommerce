package com.devfat.mini_ecommerce.home.dto;

import java.math.BigDecimal;

public record HomeStatsDto(
        long totalOrdersDelivered,
        BigDecimal averageRating,
        long totalReviews
) {
}
