package com.devfat.mini_ecommerce.product;

import com.devfat.mini_ecommerce.product.dto.ProductSearchCriteria;
import com.devfat.mini_ecommerce.product.internal.ProductEntity;
import com.devfat.mini_ecommerce.product.specification.ProductSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductSpecificationTest {

    @Test
    @DisplayName("Should create non-null specification when criteria is provided")
    void shouldCreateNonNullSpecification() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "tôm",
                List.of(1L, 2L),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(500000),
                true
        );

        Specification<ProductEntity> spec = ProductSpecification.withCriteria(criteria);
        assertNotNull(spec, "ProductSpecification should not be null");
    }

    @Test
    @DisplayName("Should normalize search string and handle null criteria gracefully")
    void shouldHandleNullAndEmptySearch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "   ",
                null,
                null,
                null,
                null
        );

        assertNull(criteria.search(), "Empty search string should be normalized to null");
        Specification<ProductEntity> spec = ProductSpecification.withCriteria(criteria);
        assertNotNull(spec, "Specification should still be created with active filter");
    }
}
