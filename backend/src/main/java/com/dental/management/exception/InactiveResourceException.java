package com.dental.management.exception;

/**
 * Exception thrown when attempting to book an appointment with an inactive dentist or treatment.
 */
public class InactiveResourceException extends RuntimeException {

    public InactiveResourceException(String message) {
        super(message);
    }
}
