package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.common.PageResponse;
import com.devfat.mini_ecommerce.dto.request.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.dto.response.CategoryResponseDto;
import com.devfat.mini_ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
@Tag(name = "Category", description = "Loại sản phẩm")
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        CategoryResponseDto categoryResponse = categoryService.create(createCategoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        categoryResponse,
                        "Create Category Successfully!"
                ));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponseDto>>> getCategories(
            @RequestParam(required = false, defaultValue = "") String search,
            Pageable pageable) {

        Page<CategoryResponseDto> categoryResponse = categoryService.findByNameContainingIgnoreCase(search, pageable);

        return ResponseEntity.ok(ApiResponse.success(
               PageResponse.of(categoryResponse),
                "Get Category Successfully!"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoriesById(@PathVariable Long id) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        categoryService.findById(id),
                        "Get Category Successfully!"
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
           @PathVariable Long id, @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        CategoryResponseDto categoryResponse = categoryService.update(id, createCategoryRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        categoryResponse,
                        "Update Category Successfully!"
                )
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteCategory(@PathVariable Long id) {
        boolean isDeletedCategory = categoryService.deleteById(id);
        return ResponseEntity.ok().body(
                ApiResponse.success(isDeletedCategory, "Delete Category Successfully!")
        );
    }
}
