package com.devfat.mini_ecommerce.home.herobanner;

import com.devfat.mini_ecommerce.home.herobanner.dto.CreateHeroBannerRequestDto;
import com.devfat.mini_ecommerce.home.herobanner.dto.HeroBannerResponseDto;
import com.devfat.mini_ecommerce.home.herobanner.dto.UpdateHeroBannerRequestDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HeroBannerService {
    List<HeroBannerResponseDto> getAllActiveBanners();  // Giữ nguyên — cho Public Home
    List<HeroBannerResponseDto> getAllBanners();

    HeroBannerResponseDto getById(Long id);
    HeroBannerResponseDto create(CreateHeroBannerRequestDto request);
    HeroBannerResponseDto update(Long id, UpdateHeroBannerRequestDto request);
    void delete(Long id);
    void toggleBannerActive(Long id);
    HeroBannerResponseDto uploadBannerImage(Long id, MultipartFile file);

}
