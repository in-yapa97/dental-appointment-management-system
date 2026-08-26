package com.dental.management.exception;

/**
 * Exception thrown when attempting to delete a bill that cannot be removed (e.g. status is PAID).
 */
public class BillDeletionException extends RuntimeException {

    public BillDeletionException(String message) {
        super(message);
    }
}
