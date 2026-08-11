package com.devfat.mini_ecommerce.auth.exception;

import com.devfat.mini_ecommerce.shared.exception.BadRequestException;











public class OtpInvalidException extends BadRequestException {
    public OtpInvalidException(String message) {
        super(message);
    }
}
