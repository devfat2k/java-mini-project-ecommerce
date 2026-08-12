package com.devfat.mini_ecommerce.home.herobanner.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HeroBannerRepository extends JpaRepository<HeroBannerEntity, Long> {
    List<HeroBannerEntity> findByIsActiveTrueOrderBySortOrderAsc();
}
