package com.devfat.mini_ecommerce.shared.exception;








import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
