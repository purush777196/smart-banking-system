package com.finova.exception;

public class UnauthorizedCustomerAccessException extends RuntimeException {
    public UnauthorizedCustomerAccessException(String message) {
        super(message);
    }
}

