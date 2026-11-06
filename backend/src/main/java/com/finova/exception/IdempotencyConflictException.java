package com.finova.exception;

/**
 * Thrown when an idempotency key is reused with mismatched operation parameters (e.g., different amount, account, or transaction type).
 */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }

    public IdempotencyConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

