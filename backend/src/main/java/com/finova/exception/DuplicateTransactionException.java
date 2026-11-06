package com.finova.exception;

/**
 * Thrown when a concurrent collision on an idempotency key or reference occurs.
 */
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }

    public DuplicateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}

