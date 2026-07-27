package com.devfat.mini_ecommerce.exception;

public class OtpExpiredException extends OtpInvalidException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
