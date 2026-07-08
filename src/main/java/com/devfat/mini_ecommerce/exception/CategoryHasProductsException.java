package com.devfat.mini_ecommerce.exception;

public class CategoryHasProductsException extends RuntimeException {
    public CategoryHasProductsException(String message) {
        super(message);
    }
}