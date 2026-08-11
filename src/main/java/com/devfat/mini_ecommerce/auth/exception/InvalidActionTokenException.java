package com.devfat.mini_ecommerce.auth.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;







import org.springframework.http.HttpStatus;

public class InvalidActionTokenException extends BusinessException {
    public InvalidActionTokenException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
