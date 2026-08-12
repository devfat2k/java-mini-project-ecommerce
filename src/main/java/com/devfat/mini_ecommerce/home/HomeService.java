package com.devfat.mini_ecommerce.home;

import com.devfat.mini_ecommerce.home.dto.*;

import java.util.List;

public interface HomeService {
    HomePageDataDto getHomePageData();
    void evictAllHomeCaches();
    List<HeroSlideDto> getHeroSlides();
    List<HomeCategoryDto> getHomeCategories();
    List<DailyArrivalDto> getDailyArrivals();
    List<FeaturedProductDto> getFeaturedProducts();
    List<FeaturedProductTabDto> getFeaturedProductTabs();
    List<ComboSetDto> getComboSets();
    List<FeaturedReviewDto> getFeaturedReviews();
    HomeStatsDto getStats();
}
