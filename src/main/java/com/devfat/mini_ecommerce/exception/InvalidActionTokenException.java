package com.devfat.mini_ecommerce.exception;

public class InvalidActionTokenException extends RuntimeException {

    public InvalidActionTokenException(String message) {
        super(message);
    }
}