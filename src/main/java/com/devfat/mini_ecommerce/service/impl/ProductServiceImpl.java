package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.response.ProductResponseDto;
import com.devfat.mini_ecommerce.entity.ProductEntity;
import com.devfat.mini_ecommerce.repository.ProductRepository;
import com.devfat.mini_ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private ProductResponseDto toResponse(ProductEntity productEntity) {
       return ProductResponseDto.builder()
               .id(productEntity.getId())
               .name(productEntity.getName())
               .price(productEntity.getPrice())
               .stock(productEntity.getStock())
               .categoryName(productEntity.getCategory() != null ? productEntity.getCategory().getName() : null)
               .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAll(Pageable pageable) {
        return productRepository.findByStockGreaterThan(0, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto findById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy id = " + id));
    }

    @Transactional
    public ProductEntity decreaseStock(Long productId, int quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy id = " + productId));

        if(product.getStock() < quantity) {
            throw new RuntimeException("Không đủ số lượng");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return product;
    }

    @Override
    public ProductEntity create(ProductEntity product) {
        return null;
    }


}
