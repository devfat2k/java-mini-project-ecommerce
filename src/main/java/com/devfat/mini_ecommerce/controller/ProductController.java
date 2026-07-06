package com.devfat.mini_ecommerce.controller;
import com.devfat.mini_ecommerce.dto.request.CreateProductRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateProductRequestDto;
import com.devfat.mini_ecommerce.dto.response.ProductResponseDto;
import com.devfat.mini_ecommerce.repository.ProductRepository;
import com.devfat.mini_ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Manager")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    @Operation(
            summary = "Create product",
            description = "Create a new product using the request body."
    )
    @PostMapping()
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody()CreateProductRequestDto createProductRequest
    ) {
        ProductResponseDto productResponseDto = productService.create(createProductRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
    }

    @Operation(
            summary = "Get products",
            description = "Retrieve products with pagination, search and sorting."
    )
    @GetMapping
    public Page<ProductResponseDto> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortBy = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return productService.getProductsWithSearch(search, pageable);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a product by its ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(
            summary = "Update product",
            description = "Update one or more product fields. Only provided fields will be updated."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDto updateProductRequest
    ) {
        ProductResponseDto productResponse = productService.update(id, updateProductRequest);
        return ResponseEntity.status(HttpStatus.OK).body(productResponse);
    }

    @Operation(
            summary = "Soft delete product",
            description = "Mark the product as inactive."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteProduct(
            @PathVariable Long id
    ) {
        boolean isSoftDelete = productService.softDelete(id);
        return ResponseEntity.status(HttpStatus.OK).body(isSoftDelete);
    }

    @Operation(
            summary = "Increase product stock",
            description = "Increase the stock quantity of a product."
    )
    @PatchMapping("/increase/{id}")
    public ResponseEntity<ProductResponseDto> increaseStock(
            @PathVariable Long id, @RequestParam int quantity
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.increaseStock(id, quantity));
    }

    @Operation(
            summary = "Decrease product stock",
            description = "Decrease the stock quantity of a product."
    )
    @PatchMapping("/decrease/{id}")
    public ResponseEntity<ProductResponseDto> decreaseStock(
            @PathVariable Long id, @RequestParam int quantity
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.decreaseStock(id, quantity));
    }

    @GetMapping("/top-buy")
    public ResponseEntity<List<ProductRepository.TopProductView>> getTopBuyProduct(
            @RequestParam  int limit
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getTopProducts(limit));
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<List<ProductRepository.CategoryRevenueView>> getRevenueByCategory() {
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.getCategoryRevenue(PageRequest.of(0, 10)));
    }

    @GetMapping("/revenue-in-month")
    public ResponseEntity<List<ProductRepository.MonthlyRevenueView>> getMonthlyRevenue() {
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.getMonthlyRevenue());
    }
}
