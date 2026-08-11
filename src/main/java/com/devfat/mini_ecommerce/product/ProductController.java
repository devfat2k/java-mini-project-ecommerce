package com.devfat.mini_ecommerce.product;

import com.devfat.mini_ecommerce.product.dto.ProductResponseDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimit;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimitType;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/products")
@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Manager")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get products",
            description = "Retrieve products with pagination, search and sorting."
    )
    @SecurityRequirements({})
    @RateLimit(type = RateLimitType.PUBLIC_API, byIp = true)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {

        PageResponse<ProductResponseDto> pageResponse = productService.getProductsWithSearch(search, categoryId, pageable);

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
    @RateLimit(type = RateLimitType.PUBLIC_API, byIp = true)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        productService.findById(id),
                        "Get Product Success"
                ));
    }

    @Operation(
            summary = "Decrease product stock",
            description = "Decrease the stock quantity of a product."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/decrease/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> decreaseStock(
            @PathVariable Long id, @RequestParam int quantity
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                productService.decreaseStock(id, quantity),
                "Decrease Stock Successfully!"
        ));
    }
}
