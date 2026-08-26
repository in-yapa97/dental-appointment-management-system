package com.dental.management.exception;

/**
 * Exception thrown when attempting to create a second bill for an appointment that already has one.
 */
public class DuplicateBillException extends RuntimeException {

    public DuplicateBillException(String message) {
        super(message);
    }
}
