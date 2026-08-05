package com.devfat.mini_ecommerce.auth.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;







import org.springframework.http.HttpStatus;

public class OtpNotFoundException extends BusinessException {
    public OtpNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
