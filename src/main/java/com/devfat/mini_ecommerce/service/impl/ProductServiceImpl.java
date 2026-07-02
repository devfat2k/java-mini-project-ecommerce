package com.devfat.mini_ecommerce.service.impl;

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

    @Override
    @Transactional(readOnly = true)
    public Page<ProductEntity> getAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductEntity findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy id = " + id));
    }

    @Override
    @Transactional
    public ProductEntity decreaseStock(Long productId, int quantity) {
        ProductEntity product = findById(productId);

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
