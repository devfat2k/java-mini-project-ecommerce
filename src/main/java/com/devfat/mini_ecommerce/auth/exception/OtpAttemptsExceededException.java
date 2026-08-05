package com.devfat.mini_ecommerce.auth.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;







import org.springframework.http.HttpStatus;

public class OtpAttemptsExceededException extends BusinessException {
    public OtpAttemptsExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
