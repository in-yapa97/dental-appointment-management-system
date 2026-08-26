package com.dental.management.exception;

/**
 * Exception thrown when a patient cannot be deleted due to existing relationships (e.g. appointments).
 */
public class PatientDeletionException extends RuntimeException {

    public PatientDeletionException(String message) {
        super(message);
    }
}
