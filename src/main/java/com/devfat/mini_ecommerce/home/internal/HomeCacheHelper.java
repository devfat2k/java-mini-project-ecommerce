package com.devfat.mini_ecommerce.home.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryRepository;
import com.devfat.mini_ecommerce.home.dailyarrival.DailyArrivalService;
import com.devfat.mini_ecommerce.home.dto.*;
import com.devfat.mini_ecommerce.home.herobanner.HeroBannerService;
import com.devfat.mini_ecommerce.order.OrderStatus;
import com.devfat.mini_ecommerce.order.internal.OrderRepository;
import com.devfat.mini_ecommerce.product.internal.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeCacheHelper {

    private final HeroBannerService heroBannerService;
    private final DailyArrivalService dailyArrivalService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final HomeMapper homeMapper;

    @Cacheable(value = "home:heroSlides", key = "'all'")
    @Transactional(readOnly = true)
    public List<HeroSlideDto> getHeroSlides() {
        return heroBannerService.getAllActiveBanners()
                .stream()
                .map(homeMapper::toHeroSlideDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "home:categories", key = "'all'")
    @Transactional(readOnly = true)
    public List<HomeCategoryDto> getHomeCategories() {
        return categoryRepository.findByHomeIsActiveTrueOrderByHomeSortOrderAsc()
                .stream()
                .map(homeMapper::toHomeCategoryDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "home:dailyArrivals", key = "T(java.time.LocalDate).now().toString()")
    @Transactional(readOnly = true)
    public List<DailyArrivalDto> getDailyArrivals() {
        return dailyArrivalService.getByDate(LocalDate.now())
                .stream()
                .map(homeMapper::toDailyArrivalDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "home:featuredProducts", key = "'all'")
    @Transactional(readOnly = true)
    public List<FeaturedProductDto> getFeaturedProducts() {
        return productRepository.findFeaturedActiveProducts()
                .stream()
                .map(homeMapper::toFeaturedProductDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "home:featuredProductTabs", key = "'all'")
    @Transactional(readOnly = true)
    public List<FeaturedProductTabDto> getFeaturedProductTabs() {
        return new java.util.ArrayList<>(List.of(
                new FeaturedProductTabDto("all", "Tất cả", 0),
                new FeaturedProductTabDto("ca-bien-tuoi", "Cá Biển Tươi", 1),
                new FeaturedProductTabDto("tom", "Tôm Các Loại", 2),
                new FeaturedProductTabDto("muc", "Mực Tươi", 3),
                new FeaturedProductTabDto("cua-ghe", "Cua - Ghẹ", 4),
                new FeaturedProductTabDto("so-ngheu-oc", "Sò - Nghêu - Ốc", 5),
                new FeaturedProductTabDto("hai-san-kho", "Hải Sản Khô", 6),
                new FeaturedProductTabDto("nuoc-mam-gia-vi-bien", "Nước Mắm & Gia Vị Biển", 7)
        ));
    }

    @Cacheable(value = "home:comboSets", key = "'all'")
    @Transactional(readOnly = true)
    public List<ComboSetDto> getComboSets() {
        return productRepository.findActiveComboProducts()
                .stream()
                .map(homeMapper::toComboSetDto)
                .collect(Collectors.toCollection(java.util.ArrayList::new));
    }

    @Cacheable(value = "home:featuredReviews", key = "'all'")
    @Transactional(readOnly = true)
    public List<FeaturedReviewDto> getFeaturedReviews() {
        return new java.util.ArrayList<>(); // Tạm — chờ module Review
    }

    @Cacheable(value = "home:stats", key = "'all'")
    @Transactional(readOnly = true)
    public HomeStatsDto getStats() {
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DONE);
        return new HomeStatsDto(
                deliveredOrders > 0 ? deliveredOrders : 1250L,
                new BigDecimal("5.0"),
                0L
        );
    }
}
