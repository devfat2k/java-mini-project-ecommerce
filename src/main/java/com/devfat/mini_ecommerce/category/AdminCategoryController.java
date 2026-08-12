package com.devfat.mini_ecommerce.category;

import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;
import com.devfat.mini_ecommerce.category.dto.ConfigureCategoryHomeRequestDto;
import com.devfat.mini_ecommerce.category.dto.CreateCategoryRequestDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Category", description = "Admin Category Management APIs")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Create category", description = "Create a new product category.")
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

    @Operation(summary = "Update category", description = "Update an existing category by ID.")
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


    @Operation(summary = "Delete category", description = "Delete a category by ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteCategory(@PathVariable Long id) {
        boolean isDeletedCategory = categoryService.deleteById(id);
        return ResponseEntity.ok().body(
                ApiResponse.success(isDeletedCategory, "Delete Category Successfully!")
        );
    }

    @Operation(summary = "Upload category image", description = "Upload an image file for a category.")
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategoryImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        CategoryResponseDto categoryResponse = categoryService.uploadCategoryImage(id, file);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        categoryResponse,
                        "Upload Category Image Successfully!"
                )
        );
    }

    @Operation(summary = "Configure category home display", description = "Admin API — Set bento style, badge, icon, and sort order for home page category.")
    @PatchMapping("/{id}/home-config")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> configureHome(
            @PathVariable Long id,
            @Valid @RequestBody ConfigureCategoryHomeRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.configureHome(id, request),
                "Configure category home display successfully"
        ));
    }
}
