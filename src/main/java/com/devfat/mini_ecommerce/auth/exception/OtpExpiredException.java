package com.devfat.mini_ecommerce.auth.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;







import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BusinessException {
    public OtpExpiredException(String message) {
        super(message, HttpStatus.GONE);
    }
}
