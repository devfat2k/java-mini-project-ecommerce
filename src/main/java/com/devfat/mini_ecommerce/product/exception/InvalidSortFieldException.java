package com.devfat.mini_ecommerce.product.exception;

import com.devfat.mini_ecommerce.shared.exception.BadRequestException;

public class InvalidSortFieldException extends BadRequestException {
    public InvalidSortFieldException(String message) {
        super("Invalid sort field: " + " Allowed fields: [price, createdAt, name]");
    }
}
