package com.devfat.mini_ecommerce.service;
import com.devfat.mini_ecommerce.dto.request.CreateProductRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateProductRequestDto;
import com.devfat.mini_ecommerce.dto.response.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponseDto> getProductsWithSearch(String search, Pageable pageable);
    ProductResponseDto findById(Long id);
    ProductResponseDto create(CreateProductRequestDto createProductRequestDto);
    ProductResponseDto update(Long id, UpdateProductRequestDto updateProductRequestDto);
    Boolean softDelete(Long id);
    ProductResponseDto decreaseStock(Long id, int quantity);
    ProductResponseDto increaseStock(Long id, int quantity);
}
