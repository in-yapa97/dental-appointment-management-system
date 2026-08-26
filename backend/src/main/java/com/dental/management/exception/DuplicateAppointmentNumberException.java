package com.dental.management.exception;

/**
 * Exception thrown when attempting to use an already registered appointment number.
 */
public class DuplicateAppointmentNumberException extends RuntimeException {

    public DuplicateAppointmentNumberException(String message) {
        super(message);
    }
}
