package com.devfat.mini_ecommerce.home.herobanner.dto;

import java.math.BigDecimal;

public record UpdateHeroBannerRequestDto(
        Integer sortOrder,
        Boolean isActive,
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
        String cardSubtitle
) {
}
