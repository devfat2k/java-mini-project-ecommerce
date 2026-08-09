package com.devfat.mini_ecommerce.category.internal;

import com.devfat.mini_ecommerce.category.CategoryService;
import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;
import com.devfat.mini_ecommerce.category.dto.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.category.exception.CategoryHasProductsException;
import com.devfat.mini_ecommerce.shared.exception.ResourceNotFoundException;










import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public boolean existsByName(String categoryName) {
        return categoryRepository.existsByNameIgnoreCase(categoryName);
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
        return  categoryMapper.toResponseDto(categoryRepository.save(categoryEntity));

    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(categoryMapper::toResponseDto);
    }


    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category is not found. Id = " + id));
    }


    @Override
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponseDto update(Long id,CreateCategoryRequestDto createCategoryRequestDto) {
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category is not found. id = " + id));
        categoryEntity.setName(createCategoryRequestDto.name());
        return  categoryMapper.toResponseDto(categoryRepository.save(categoryEntity));
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
