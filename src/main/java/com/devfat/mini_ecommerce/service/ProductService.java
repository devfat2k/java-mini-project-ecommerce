package com.devfat.mini_ecommerce.service;
import com.devfat.mini_ecommerce.dto.response.ProductResponseDto;
import com.devfat.mini_ecommerce.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponseDto> getAll(Pageable pageable);
    ProductResponseDto findById(Long id);
//    ProductEntity decreaseStock(Long productId, int quantity);
    ProductEntity create(ProductEntity product);
}
