package com.devfat.mini_ecommerce.auth.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;







import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
