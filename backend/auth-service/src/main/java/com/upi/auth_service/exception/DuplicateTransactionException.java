package com.upi.auth_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request with a previously used idempotency key is detected,
 * indicating a duplicate transaction attempt.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateTransactionException extends BusinessException {

    public DuplicateTransactionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
