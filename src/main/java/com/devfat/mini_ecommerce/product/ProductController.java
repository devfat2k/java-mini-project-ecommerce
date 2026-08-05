package com.devfat.mini_ecommerce.product;

import com.devfat.mini_ecommerce.product.dto.CreateProductRequestDto;
import com.devfat.mini_ecommerce.product.dto.ProductResponseDto;
import com.devfat.mini_ecommerce.product.dto.UpdateProductRequestDto;
import com.devfat.mini_ecommerce.product.internal.ProductRepository;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.base.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1/products")
@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Manager")
public class ProductController {

    private final ProductService productService;


    @Operation(
            summary = "Create product",
            description = "Create a new product using the request body."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody()CreateProductRequestDto createProductRequest
    ) {
        ProductResponseDto productResponseDto = productService.create(createProductRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                productResponseDto,
                "Create Product Successfully!"
        ));
    }


    @Operation(
            summary = "Get products",
            description = "Retrieve products with pagination, search and sorting."
    )
    @SecurityRequirements({})
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getAll(
            @RequestParam(required = false, defaultValue = "") String search,
            Pageable pageable) {

        PageResponse<ProductResponseDto> pageResponse = productService.getProductsWithSearch(search, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                pageResponse,
                "Get product successfully"
        ));
    }


    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a product by its ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        productService.findById(id),
                        "Get Product Success"
                ));
    }


    @Operation(
            summary = "Update product",
            description = "Update one or more product fields. Only provided fields will be updated."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDto updateProductRequest
    ) {
        ProductResponseDto productResponse = productService.update(id, updateProductRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                productResponse,
                "Update Product Successfully!"
        ));
    }

    //============================================================//
    @Operation(
            summary = "Soft delete product",
            description = "Mark the product as inactive."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteProduct(
            @PathVariable Long id
    ) {

        boolean isSoftDelete = productService.softDelete(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                isSoftDelete,
                "Delete product successfully!"
        ));
    }

    //============================================================//
    @Operation(
            summary = "Increase product stock",
            description = "Increase the stock quantity of a product."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/increase/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> increaseStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                productService.increaseStock(id, quantity),
                "Increase Stock Successfully!"
        ));
    }

    //============================================================//
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


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top-buy")
    public ResponseEntity<ApiResponse<List<ProductRepository.TopProductView>>> getTopBuyProduct(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok().body(ApiResponse.success(
                productService.getTopProducts(limit),
                "Get Top Product Successfully!"
        ));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue-by-category")
    public ResponseEntity<ApiResponse<List<ProductRepository.CategoryRevenueView>>> getRevenueByCategory() {
        return ResponseEntity.ok().body(ApiResponse.success(
                productService.getCategoryRevenue(PageRequest.of(0, 10)),
                "Get Revenue By Category Success!"
        ));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue-in-month")
    public ResponseEntity<ApiResponse<List<ProductRepository.MonthlyRevenueView>>> getMonthlyRevenue() {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        productService.getMonthlyRevenue(),
                        "Get Monthly Revenue Success!"
                ));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductResponseDto>> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                productService.uploadProductImage(id, file),
                "Upload Image Product Successfully!"
        ));
    }
}
