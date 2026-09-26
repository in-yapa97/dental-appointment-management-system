package com.dental.management.exception;

/**
 * Thrown when attempting to delete a dentist who has existing appointments attached.
 */
public class DentistDeletionException extends RuntimeException {

    public DentistDeletionException(String message) {
        super(message);
    }
}
