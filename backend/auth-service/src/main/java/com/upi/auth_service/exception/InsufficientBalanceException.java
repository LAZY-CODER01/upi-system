package com.upi.auth_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a wallet does not have enough funds to complete a transfer.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
