package com.devfat.mini_ecommerce.category.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;







import org.springframework.http.HttpStatus;

public class CategoryHasProductsException extends BusinessException {
    public CategoryHasProductsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
