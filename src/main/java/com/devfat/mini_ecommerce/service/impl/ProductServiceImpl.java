package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.CreateProductRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateProductRequestDto;
import com.devfat.mini_ecommerce.dto.response.CategoryResponseDto;
import com.devfat.mini_ecommerce.dto.response.ProductResponseDto;
import com.devfat.mini_ecommerce.entity.CategoryEntity;
import com.devfat.mini_ecommerce.entity.ProductEntity;
import com.devfat.mini_ecommerce.exception.BadRequestException;
import com.devfat.mini_ecommerce.exception.InsufficientStockException;
import com.devfat.mini_ecommerce.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.mapper.ProductMapper;
import com.devfat.mini_ecommerce.repository.CategoryRepository;
import com.devfat.mini_ecommerce.repository.ProductRepository;
import com.devfat.mini_ecommerce.service.ProductService;
import com.devfat.mini_ecommerce.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final StorageService storageService;
    private final ProductMapper productMapper;

//    private ProductResponseDto toResponse(ProductEntity productEntity) {
//        CategoryResponseDto categoryDto = productEntity.getCategory() != null
//                ? CategoryResponseDto.builder()
//                .id(productEntity.getCategory().getId())
//                .categoryName(productEntity.getCategory().getName())
//                .build()
//                : null;
//       return ProductResponseDto.builder()
//               .id(productEntity.getId())
//               .name(productEntity.getName())
//               .imageUrl(productEntity.getImageUrl())
//               .description(productEntity.getDescription())
//               .active(productEntity.isActive())
//               .price(productEntity.getPrice())
//               .stock(productEntity.getStock())
//               .category(categoryDto) .build();
//    }

    @Override
    @Transactional
    public ProductResponseDto create(CreateProductRequestDto createProductRequestDto) {
        CategoryEntity category = categoryRepository.findById(createProductRequestDto.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Category id is not found"));
        ProductEntity product = new ProductEntity();
        product.setName(createProductRequestDto.name());
        product.setCategory(category);
        product.setDescription(createProductRequestDto.description());
        product.setPrice(createProductRequestDto.price());
        product.setStock(createProductRequestDto.stock());
        product.setActive(createProductRequestDto.isActive());

//        return toResponse(productRepository.save(product));
        return productMapper.toResponseDto(productRepository.save(product));
    }

    @Override
    @Cacheable(value = "categories",
            key = "#search + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsWithSearch(String search, Pageable pageable) {
        return productRepository.findByNameContainsIgnoreCase(search, pageable)
                .map(productMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponseDto findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy id = " + id));
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public ProductResponseDto update(Long id, UpdateProductRequestDto updateProductRequest) {
        ProductEntity product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product id is not found"));

        if(updateProductRequest.name() != null) product.setName(updateProductRequest.name());
        if(updateProductRequest.description() != null)  product.setDescription(updateProductRequest.description());
        if(updateProductRequest.stock() != null) product.setStock(updateProductRequest.stock());
        if (updateProductRequest.isActive() != null) product.setActive(updateProductRequest.isActive());

        if(updateProductRequest.price() != null) {
            if(updateProductRequest.price().equals(BigDecimal.ZERO)) throw new ResourceNotFoundException("Price must be greater than 0");
            product.setPrice(updateProductRequest.price());
        }

        if(updateProductRequest.categoryId() != null) {
            CategoryEntity category = categoryRepository.findById(updateProductRequest.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Category id is not found"));
            product.setCategory(category);
        }

//        return toResponse(productRepository.save(product));
        return productMapper.toResponseDto(productRepository.save(product));
    }


    @Override
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public ProductResponseDto decreaseStock(Long id, int quantity) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found id = " + id));

        // TODO: must add retry tại đây, để xử lý khi có lỗi hoặc đồng thời cao để tương tác sau đó ném lỗi ~ 3 lần retry

        if(quantity <= 0) throw new BadRequestException("Quantity must be greater than 0");
        if(product.getStock() < quantity) throw new IllegalArgumentException("The product is unavailable. Please try again or choose another product.");

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

//        return toResponse(product);
        return productMapper.toResponseDto(product);
    }


    @Override
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public ProductResponseDto increaseStock(Long id, int quantity) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product id is not found"));
        if(quantity <= 0) throw new InsufficientStockException("Quantity must than 0");

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

//        return toResponse(product);
        return productMapper.toResponseDto(product);
    }


    @Override
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public Boolean softDelete(Long id) {
        ProductEntity product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product id is not found"));
        if(!(product.isActive())) throw new ResourceNotFoundException("Products is unactive");

        product.setActive(false);
        productRepository.save(product);

        return true;
    }

    @Override
    @Cacheable(value = "analytics", key = "'top-products'")
    @Transactional(readOnly = true)
    public List<ProductRepository.TopProductView> getTopProducts(int limit) {
        return productRepository.getTopViewProduct(PageRequest.of(0, limit));
    }

    @Override
    @Cacheable(value = "analytics", key = "'category-revenue'")
    @Transactional(readOnly = true)
    public List<ProductRepository.CategoryRevenueView> getCategoryRevenue(Pageable pageable) {
        return productRepository.getCategoryRevenue(pageable);
    }

    @Override
    @Cacheable(value = "analytics", key = "'monthly-revenue'")
    @Transactional(readOnly = true)
    public List<ProductRepository.MonthlyRevenueView> getMonthlyRevenue() {
        return productRepository.getMonthlyRevenue();
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    @Transactional  // ← Bắt buộc: giữ session mở cho đến khi toResponse() truy cập category (LAZY)
    public ProductResponseDto uploadProductImage(Long id, MultipartFile file) {
       ProductEntity product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product id is not found"));

        String url = storageService.uploadFile(file,"productImage", true);
        product.setImageUrl(url);
        productRepository.save(product);

//        return toResponse(product);
        return  productMapper.toResponseDto(product);
    }
}
