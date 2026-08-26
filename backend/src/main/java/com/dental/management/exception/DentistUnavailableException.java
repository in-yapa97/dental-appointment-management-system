package com.dental.management.exception;

/**
 * Exception thrown when a dentist is already booked or unavailable for a requested time slot.
 */
public class DentistUnavailableException extends RuntimeException {

    public DentistUnavailableException(String message) {
        super(message);
    }
}
