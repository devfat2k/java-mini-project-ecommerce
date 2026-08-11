package com.devfat.mini_ecommerce.user.address.exception;

import com.devfat.mini_ecommerce.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CannotDeleteOnlyAddressException extends BusinessException {
    public CannotDeleteOnlyAddressException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
