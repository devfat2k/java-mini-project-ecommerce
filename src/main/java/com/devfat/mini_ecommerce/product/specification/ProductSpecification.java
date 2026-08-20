package com.devfat.mini_ecommerce.product.specification;

import com.devfat.mini_ecommerce.product.dto.ProductSearchCriteria;
import com.devfat.mini_ecommerce.product.internal.ProductEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;


public final class ProductSpecification {
    private ProductSpecification() {}


    public static Specification<ProductEntity> withCriteria(ProductSearchCriteria criteria) {
        return Specification.allOf(
                hasSearch(criteria.search()),
                hasCategoryIn(criteria.categoryId()),
                hasMinPrice(criteria.minPrice()),
                hasMaxPrice(criteria.maxPrice()),
                isInStock(criteria.inStock()),
                isActive()
        );
    }

    private static Specification<ProductEntity> hasMaxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private static Specification<ProductEntity> hasMinPrice(BigDecimal minPrice) {
        if (minPrice == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<ProductEntity> hasCategoryIn(List<Long> categoryIds) {
        if(categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    private static Specification<ProductEntity> hasSearch(String search) {
        if(!StringUtils.hasText(search)) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return ((root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern));
    }

    private static Specification<ProductEntity> isInStock(Boolean inStock) {
        if (inStock == null || !inStock) return null;
        return (root, query, cb) -> cb.greaterThan(root.get("stock"), 0);
    }

    private static Specification<ProductEntity> isActive() {
        return (root, query, cb) -> cb.equal(root.get("isActive"), true);
    }

}
