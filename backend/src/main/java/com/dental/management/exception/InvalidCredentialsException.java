package com.dental.management.exception;

/**
 * Exception thrown when authentication fails due to invalid username or password.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
