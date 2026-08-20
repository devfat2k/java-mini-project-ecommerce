package com.devfat.mini_ecommerce.product.exception;

import com.devfat.mini_ecommerce.shared.exception.BadRequestException;

public class InvalidPriceRangeException extends BadRequestException {
    public InvalidPriceRangeException(String field) {
         super("Max or min '" + field + "' is out of range.");
    }
}
