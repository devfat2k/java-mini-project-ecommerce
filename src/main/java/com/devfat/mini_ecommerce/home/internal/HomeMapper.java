package com.devfat.mini_ecommerce.home.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryEntity;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.DailyArrivalResponseDto;
import com.devfat.mini_ecommerce.home.dto.*;
import com.devfat.mini_ecommerce.home.herobanner.dto.HeroBannerResponseDto;
import com.devfat.mini_ecommerce.product.internal.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface HomeMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(dto.id()))")
    @Mapping(target = "badge", source = "dto", qualifiedByName = "toHeroBadgeDto")
    @Mapping(target = "primaryCta", source = "dto", qualifiedByName = "toPrimaryCtaDto")
    @Mapping(target = "secondaryCta", source = "dto", qualifiedByName = "toSecondaryCtaDto")
    @Mapping(target = "productCard", source = "dto", qualifiedByName = "toProductCardDto")
    HeroSlideDto toHeroSlideDto(HeroBannerResponseDto dto);

    @Named("toHeroBadgeDto")
    default HeroBadgeDto toHeroBadgeDto(HeroBannerResponseDto dto) {
        if (dto.badgeText() == null && dto.badgeIcon() == null) return null;
        return new HeroBadgeDto(dto.badgeText(), dto.badgeIcon());
    }

    @Named("toPrimaryCtaDto")
    default HeroCtaDto toPrimaryCtaDto(HeroBannerResponseDto dto) {
        if (dto.primaryCtaLabel() == null) return null;
        return new HeroCtaDto(dto.primaryCtaLabel(), dto.primaryCtaHref(), dto.primaryCtaIcon());
    }

    @Named("toSecondaryCtaDto")
    default HeroCtaDto toSecondaryCtaDto(HeroBannerResponseDto dto) {
        if (dto.secondaryCtaLabel() == null) return null;
        return new HeroCtaDto(dto.secondaryCtaLabel(), dto.secondaryCtaHref(), dto.secondaryCtaIcon());
    }

    @Named("toProductCardDto")
    default HeroProductCardDto toProductCardDto(HeroBannerResponseDto dto) {
        return new HeroProductCardDto(
                dto.cardImageUrl(),
                dto.cardImageAlt(),
                dto.cardComboBadge(),
                dto.cardDiscountBadge(),
                dto.cardOriginalPrice(),
                dto.cardSalePrice(),
                dto.cardTitle(),
                dto.cardSubtitle()
        );
    }

    DailyArrivalDto toDailyArrivalDto(DailyArrivalResponseDto dto);

    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "homeDisplayStyle", target = "displayStyle")
    @Mapping(source = "homeSortOrder", target = "sortOrder")
    @Mapping(source = "homeIsActive", target = "isActive")
    @Mapping(target = "productCount", expression = "java(entity.getProducts() != null ? (long) entity.getProducts().size() : 0L)")
    HomeCategoryDto toHomeCategoryDto(CategoryEntity entity);

    @Mapping(source = "category.name", target = "categoryLabel")
    @Mapping(source = "category.slug", target = "categorySlug")
    @Mapping(source = "tags", target = "badges")
    @Mapping(source = "averageRating", target = "rating")
    FeaturedProductDto toFeaturedProductDto(ProductEntity entity);

    @Mapping(source = "comboTag", target = "tag")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "comboCtaText", target = "ctaText")
    @Mapping(source = "comboHref", target = "href")
    @Mapping(source = "comboTheme", target = "theme")
    @Mapping(source = "comboCategory", target = "category")
    @Mapping(source = "comboSortOrder", target = "sortOrder")
    @Mapping(source = "breakout", target = "isBreakout")
    @Mapping(source = "active", target = "isActive")
    ComboSetDto toComboSetDto(ProductEntity entity);
}
