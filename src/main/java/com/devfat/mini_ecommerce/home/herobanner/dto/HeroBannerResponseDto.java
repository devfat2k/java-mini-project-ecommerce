package com.devfat.mini_ecommerce.home.herobanner.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HeroBannerResponseDto(
        Long id,
        Integer sortOrder,
        boolean isActive,
        String badgeText,
        String badgeIcon,
        String titlePrefix,
        String titleHighlight,
        String titleSuffix,
        String description,
        String primaryCtaLabel,
        String primaryCtaHref,
        String primaryCtaIcon,
        String secondaryCtaLabel,
        String secondaryCtaHref,
        String secondaryCtaIcon,
        String cardImageUrl,
        String cardImageAlt,
        String cardComboBadge,
        String cardDiscountBadge,
        BigDecimal cardOriginalPrice,
        BigDecimal cardSalePrice,
        String cardTitle,
        String cardSubtitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
