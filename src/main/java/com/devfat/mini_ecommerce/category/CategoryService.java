package com.devfat.mini_ecommerce.category;

import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;
import com.devfat.mini_ecommerce.category.dto.CreateCategoryRequestDto;










import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CategoryService {
    CategoryResponseDto create(CreateCategoryRequestDto createCategoryRequestDto);
    Page<CategoryResponseDto> findByNameContainingIgnoreCase(String name, Pageable pageable);
    CategoryResponseDto findById(Long id);
    CategoryResponseDto update(Long id, CreateCategoryRequestDto createCategoryRequestDto);
    Boolean deleteById(Long id);
}
