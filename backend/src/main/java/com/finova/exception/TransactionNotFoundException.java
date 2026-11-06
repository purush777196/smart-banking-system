package com.finova.exception;

/**
 * Thrown when a banking transaction is not found by ID or reference string.
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}

