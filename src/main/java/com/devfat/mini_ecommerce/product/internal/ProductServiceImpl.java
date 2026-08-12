package com.devfat.mini_ecommerce.product.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryEntity;
import com.devfat.mini_ecommerce.product.ProductService;
import com.devfat.mini_ecommerce.category.internal.CategoryRepository;
import com.devfat.mini_ecommerce.product.dto.CreateProductRequestDto;
import com.devfat.mini_ecommerce.product.dto.ProductResponseDto;
import com.devfat.mini_ecommerce.product.dto.ProductSearchCriteria;
import com.devfat.mini_ecommerce.product.dto.UpdateProductRequestDto;
import com.devfat.mini_ecommerce.product.exception.InsufficientStockException;
import com.devfat.mini_ecommerce.product.specification.ProductSpecification;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import com.devfat.mini_ecommerce.shared.exception.BadRequestException;
import com.devfat.mini_ecommerce.shared.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.storage.StorageService;
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

        return productMapper.toResponseDto(productRepository.save(product));
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> getProductsWithSearch(ProductSearchCriteria criteria, Pageable pageable) {
        Page<ProductResponseDto> product =
                productRepository.findAll(ProductSpecification.withCriteria(criteria), pageable)
                .map(productMapper::toResponseDto);
        return PageResponse.of(product);
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

        return  productMapper.toResponseDto(product);
    }
}
