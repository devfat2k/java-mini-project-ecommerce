package com.devfat.mini_ecommerce.home.dto;

import java.util.List;

public record HomePageDataDto(
        List<HeroSlideDto> heroSlides,
        List<HomeCategoryDto> categories,
        List<DailyArrivalDto> dailyArrivals,
        List<FeaturedProductDto> featuredProducts,
        List<FeaturedProductTabDto> featuredProductTabs,
        List<ComboSetDto> comboSets,
        List<FeaturedReviewDto> featuredReviews,
        HomeStatsDto stats
) {}
