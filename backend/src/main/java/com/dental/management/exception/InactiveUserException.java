package com.dental.management.exception;

/**
 * Exception thrown when an inactive user attempts to authenticate.
 */
public class InactiveUserException extends RuntimeException {

    public InactiveUserException(String message) {
        super(message);
    }
}
