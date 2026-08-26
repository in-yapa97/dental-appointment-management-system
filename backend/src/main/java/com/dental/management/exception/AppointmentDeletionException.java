package com.dental.management.exception;

/**
 * Exception thrown when an appointment cannot be safely deleted (e.g. associated bills exist).
 */
public class AppointmentDeletionException extends RuntimeException {

    public AppointmentDeletionException(String message) {
        super(message);
    }
}
