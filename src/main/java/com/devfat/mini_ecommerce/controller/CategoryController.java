package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.dto.request.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.dto.response.CategoryResponseDto;
import com.devfat.mini_ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
@Tag(name = "Category", description = "Loại sản phẩm")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        CategoryResponseDto categoryResponse = categoryService.create(createCategoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
    }

    @GetMapping()
    public Page<CategoryResponseDto> getCategories(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
            ) {
        Sort.Direction sortDirection = direction.contains("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy =  Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return categoryService.findByNameContainingIgnoreCase(search, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoriesById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
           @PathVariable Long id, @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        CategoryResponseDto categoryResponse = categoryService.update(id, createCategoryRequestDto);
        return ResponseEntity.ok(categoryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCategory(@PathVariable Long id) {
        boolean isDeletedCategory = categoryService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(isDeletedCategory);
    }
}
