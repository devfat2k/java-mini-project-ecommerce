package com.devfat.mini_ecommerce.shared.exception;








import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
