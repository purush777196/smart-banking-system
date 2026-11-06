package com.finova.exception;

/**
 * Thrown when attempting financial operations against non-ACTIVE (e.g. FROZEN, CLOSED) bank accounts.
 */
public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(String message) {
        super(message);
    }
}

