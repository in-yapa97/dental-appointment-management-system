package com.dental.management.exception;

/**
 * Exception thrown when attempting to use an already registered patient number.
 */
public class DuplicatePatientNumberException extends RuntimeException {

    public DuplicatePatientNumberException(String message) {
        super(message);
    }
}
