package com.dental.management.exception;

/**
 * Thrown when attempting to register or update a dentist with an already registered dentist number.
 */
public class DuplicateDentistNumberException extends RuntimeException {

    public DuplicateDentistNumberException(String message) {
        super(message);
    }
}
