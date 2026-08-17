package com.devfat.mini_ecommerce.category;

import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
@Tag(name = "Category", description = "Public Category APIs")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Get active categories", description = "Retrieve a list of all active categories.")
    @SecurityRequirements({})
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories(){
        List<CategoryResponseDto> categoryResponse = categoryService.countActiveCategories();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        categoryResponse,
                        "Get All Categories Successfully!"
                )
        );
    }

    @Operation(summary = "Get category by ID", description = "Retrieve details of a specific category by ID.")
    @SecurityRequirements({})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoriesById(@PathVariable Long id) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        categoryService.findById(id),
                        "Get Category Successfully!"
                )
        );
    }

}
