package com.devfat.mini_ecommerce.category;

import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;
import com.devfat.mini_ecommerce.category.dto.ConfigureCategoryHomeRequestDto;
import com.devfat.mini_ecommerce.category.dto.CreateCategoryRequestDto;










import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface CategoryService {
    CategoryResponseDto create(CreateCategoryRequestDto createCategoryRequestDto);
    CategoryResponseDto findById(Long id);
    CategoryResponseDto update(Long id, CreateCategoryRequestDto createCategoryRequestDto);
    Boolean deleteById(Long id);
    List<CategoryResponseDto> countActiveCategories();

    CategoryResponseDto uploadCategoryImage(Long id, MultipartFile file);
    CategoryResponseDto configureHome(Long id, ConfigureCategoryHomeRequestDto request);
}
