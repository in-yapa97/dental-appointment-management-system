package com.dental.management.exception;

/**
 * Exception thrown when an invalid payment status transition or value is encountered.
 */
public class InvalidPaymentStatusException extends RuntimeException {

    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
