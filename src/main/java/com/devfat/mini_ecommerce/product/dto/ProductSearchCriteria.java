package com.devfat.mini_ecommerce.product.dto;

import java.math.BigDecimal;
import java.util.List;


public record ProductSearchCriteria(
   String search,
   List<Long> categoryId,
   BigDecimal minPrice,
   BigDecimal maxPrice,
   Boolean inStock
) {
    public ProductSearchCriteria {
        if(search != null) {
            search = search.trim();
            if (search.isEmpty()) search = null;
        }
    }
}
