package com.devfat.mini_ecommerce.product.validation;

import com.devfat.mini_ecommerce.product.exception.InvalidSortFieldException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class ProductSortValidator {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("price", "createdAt", "name", "stock");

    public void validate(Sort sort) {
        if(sort == null || sort.isUnsorted()) return;

        for(Sort.Order order : sort) {
            String property = order.getProperty();
            if(!ALLOWED_SORT_FIELDS.contains(property)) {
                throw new InvalidSortFieldException("Sort field '" + property + "' is not supported. Allowed fields: " + ALLOWED_SORT_FIELDS);
            }
        }
    }
}
