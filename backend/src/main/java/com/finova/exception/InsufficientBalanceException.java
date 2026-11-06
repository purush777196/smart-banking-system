package com.finova.exception;

/**
 * Thrown when an account withdrawal or debit exceeds the available cleared balance.
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

