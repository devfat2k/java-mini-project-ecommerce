package com.devfat.mini_ecommerce.home.dto;

public record HeroSlideDto(
        String id,
        Integer sortOrder,
        boolean isActive,
        HeroBadgeDto badge,
        String titlePrefix,
        String titleHighlight,
        String titleSuffix,
        String description,
        HeroCtaDto primaryCta,
        HeroCtaDto secondaryCta,
        HeroProductCardDto productCard
) {}
