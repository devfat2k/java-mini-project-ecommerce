package com.devfat.mini_ecommerce.product.validation;

import com.devfat.mini_ecommerce.product.dto.ProductSearchCriteria;
import com.devfat.mini_ecommerce.product.exception.InvalidPriceRangeException;
import com.devfat.mini_ecommerce.shared.exception.BadRequestException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ProductSearchCriteriaValidator {
    public void validate(ProductSearchCriteria productSearchCriteria) {
        if(productSearchCriteria == null) return;

        BigDecimal minPrice = productSearchCriteria.minPrice();
        BigDecimal maxPrice = productSearchCriteria.maxPrice();
        String search = productSearchCriteria.search();

        if(minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceRangeException("Max or min is out of range");
        }
        if(maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceRangeException("Max or min is out of range");
        }

        if(minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidPriceRangeException("Min price is not larger than max price");
        }

        if(search != null && search.length() > 100) {
            throw new BadRequestException("Search length exceed 100");
        }
    }
}
