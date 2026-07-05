package com.devfat.mini_ecommerce.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);  // gọi constructor cha, message này sẽ lấy được qua ex.getMessage()
    }
}
