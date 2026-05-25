package com.upi.auth_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Base domain exception that carries an explicit HTTP status code.
 * Prefer throwing a more specific subclass where one exists.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
