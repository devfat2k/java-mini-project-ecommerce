package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.dto.response.CategoryResponseDto;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CategoryService {
    CategoryResponseDto create(CreateCategoryRequestDto createCategoryRequestDto);
    Page<CategoryResponseDto> findByNameContainingIgnoreCase(String name, Pageable pageable);
    CategoryResponseDto findById(Long id);
    CategoryResponseDto update(Long id, CreateCategoryRequestDto createCategoryRequestDto);
    Boolean deleteById(Long id) throws BadRequestException;
}
