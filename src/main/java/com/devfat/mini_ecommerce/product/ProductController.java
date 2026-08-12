package com.devfat.mini_ecommerce.product;

import com.devfat.mini_ecommerce.product.dto.ProductResponseDto;
import com.devfat.mini_ecommerce.product.dto.ProductSearchCriteria;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimit;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimitType;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/products")
@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Manager")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get products with search & filters",
            description = "Public API — Retrieve active products with dynamic search and multi-filtering.")
    @SecurityRequirements({})
    @RateLimit(type = RateLimitType.PUBLIC_API)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getAll(
            @ParameterObject @ModelAttribute ProductSearchCriteria criteria,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            ) {
        PageResponse<ProductResponseDto> pageResponse = productService.getProductsWithSearch(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                pageResponse,
                "Get product successfully"
        ));
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a product by its ID."
    )
    @SecurityRequirements({})
    @RateLimit(type = RateLimitType.PUBLIC_API)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        productService.findById(id),
                        "Get Product Success"
                ));
    }
}
