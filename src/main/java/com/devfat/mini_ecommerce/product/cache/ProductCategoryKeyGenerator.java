package com.devfat.mini_ecommerce.product.cache;

import com.devfat.mini_ecommerce.product.dto.ProductSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component("productCategoryKeyGenerator")
public class ProductCategoryKeyGenerator implements KeyGenerator {

    @Override
    public  @NonNull Object generate(
            @NonNull Object target,
            @NonNull Method method,
            Object... params) {
        ProductSearchCriteria c = (ProductSearchCriteria) params[0];
        Pageable p = (Pageable) params[1];

        String categoryIds = (c.categoryId() != null && !c.categoryId().isEmpty()) ? c.categoryId().toString() : "all";
        int page = p.isPaged() ? p.getPageNumber() : 0;
        int size = p.isPaged() ? p.getPageSize() : 10;
        String sort = (p.isPaged() && p.getSort().isSorted()) ? p.getSort().toString() : "default";

        return String.format("cat:%s_p:%d_s:%d_sort:%s", categoryIds, page, size, sort);
    }
}
