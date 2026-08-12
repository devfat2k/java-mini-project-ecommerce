package com.devfat.mini_ecommerce.product;

import com.devfat.mini_ecommerce.product.dto.*;
import com.devfat.mini_ecommerce.product.internal.ProductEntity;
import com.devfat.mini_ecommerce.product.internal.ProductRepository;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    PageResponse<ProductResponseDto> getProductsWithSearch(ProductSearchCriteria criteria, Pageable pageable);
    ProductResponseDto findById(Long id);
    ProductResponseDto create(CreateProductRequestDto createProductRequestDto);
    ProductResponseDto update(Long id, UpdateProductRequestDto updateProductRequestDto);
    Boolean softDelete(Long id);
    ProductResponseDto decreaseStock(Long id, int quantity);
    ProductResponseDto increaseStock(Long id, int quantity);
    List<ProductRepository.TopProductView> getTopProducts(int limit);
    List<ProductRepository.CategoryRevenueView> getCategoryRevenue(Pageable pageable);
    List<ProductRepository.MonthlyRevenueView> getMonthlyRevenue();

    ProductResponseDto uploadProductImage(Long id, MultipartFile file);

    void toggleFeaturedProduct(Long id);
    ProductResponseDto configureCombo(Long id, ConfigureProductComboRequestDto request);
}
