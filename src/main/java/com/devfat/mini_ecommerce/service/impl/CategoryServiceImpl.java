package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.dto.response.CategoryResponseDto;
import com.devfat.mini_ecommerce.entity.CategoryEntity;
import com.devfat.mini_ecommerce.exception.CategoryHasProductsException;
import com.devfat.mini_ecommerce.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.repository.CategoryRepository;
import com.devfat.mini_ecommerce.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;

    public boolean existsByName(String categoryName) {
        return categoryRepository.existsByNameIgnoreCase(categoryName);
    }

    public CategoryResponseDto toCategoryResponseDto(CategoryEntity categoryEntity) {
        return CategoryResponseDto.builder()
                .id(categoryEntity.getId())
                .categoryName(categoryEntity.getName())
                .build();
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponseDto create(CreateCategoryRequestDto createCategoryRequestDto) {
        boolean isExistsCategoryName = existsByName(createCategoryRequestDto.name());
        if(isExistsCategoryName) {
            throw new ResourceNotFoundException("Category name is already exists. Category name = " + createCategoryRequestDto.name());
        }
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(createCategoryRequestDto.name());
        return toCategoryResponseDto(categoryRepository.save(categoryEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toCategoryResponseDto);
    }


    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toCategoryResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category is not found. Id = " + id));
    }


    @Override
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponseDto update(Long id,CreateCategoryRequestDto createCategoryRequestDto) {
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category is not found. id = " + id));
        categoryEntity.setName(createCategoryRequestDto.name());
        return toCategoryResponseDto(categoryRepository.save(categoryEntity));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public Boolean deleteById(Long id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category is not found. id = " + id));
        if (!categoryEntity.getProducts().isEmpty()) {
            throw new CategoryHasProductsException("Category is had products. Do not delete any products.");
        }
        categoryRepository.delete(categoryEntity);
        return true;
    }
}
