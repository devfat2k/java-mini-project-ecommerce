package com.devfat.mini_ecommerce.home.internal;

import com.devfat.mini_ecommerce.home.HomeService;
import com.devfat.mini_ecommerce.home.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeCacheHelper cacheHelper;

    @Override
    @Transactional(readOnly = true)
    public HomePageDataDto getHomePageData() {
        return new HomePageDataDto(
                cacheHelper.getHeroSlides(),
                cacheHelper.getHomeCategories(),
                cacheHelper.getDailyArrivals(),
                cacheHelper.getFeaturedProducts(),
                cacheHelper.getFeaturedProductTabs(),
                cacheHelper.getComboSets(),
                cacheHelper.getFeaturedReviews(),
                cacheHelper.getStats()
        );
    }

    @Override
    @CacheEvict(value = {
            "home:heroSlides",
            "home:categories",
            "home:dailyArrivals",
            "home:featuredProducts",
            "home:featuredProductTabs",
            "home:comboSets",
            "home:featuredReviews",
            "home:stats"
    }, allEntries = true)
    public void evictAllHomeCaches() {
        // Body rỗng — Spring AOP sẽ tự xóa tất cả cache keys khai báo ở trên
        // @CacheEvict chạy TRƯỚC hoặc SAU method tùy config (mặc định: sau)
    }

    @Override public List<HeroSlideDto> getHeroSlides() { return cacheHelper.getHeroSlides(); }
    @Override public List<HomeCategoryDto> getHomeCategories() { return cacheHelper.getHomeCategories(); }
    @Override public List<DailyArrivalDto> getDailyArrivals() { return cacheHelper.getDailyArrivals(); }
    @Override public List<FeaturedProductDto> getFeaturedProducts() { return cacheHelper.getFeaturedProducts(); }
    @Override public List<FeaturedProductTabDto> getFeaturedProductTabs() { return cacheHelper.getFeaturedProductTabs(); }
    @Override public List<ComboSetDto> getComboSets() { return cacheHelper.getComboSets(); }
    @Override public List<FeaturedReviewDto> getFeaturedReviews() { return cacheHelper.getFeaturedReviews(); }
    @Override public HomeStatsDto getStats() { return cacheHelper.getStats(); }
}
