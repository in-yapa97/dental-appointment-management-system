package com.dental.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global REST exception handler providing standardized JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Validation Failed");
        errorBody.put("message", "Request validation failed for one or more fields");
        errorBody.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.NOT_FOUND.value());
        errorBody.put("error", "Not Found");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(DuplicatePatientNumberException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePatientNumber(DuplicatePatientNumberException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.CONFLICT.value());
        errorBody.put("error", "Patient Number Already Exists");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler(PatientDeletionException.class)
    public ResponseEntity<Map<String, Object>> handlePatientDeletion(PatientDeletionException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.CONFLICT.value());
        errorBody.put("error", "Cannot Delete Patient");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUsername(DuplicateUsernameException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.CONFLICT.value());
        errorBody.put("error", "Username Already Exists");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(Exception ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error", "Invalid Credentials");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<Map<String, Object>> handleInactiveUser(InactiveUserException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.FORBIDDEN.value());
        errorBody.put("error", "Account Inactive");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.FORBIDDEN.value());
        errorBody.put("error", "Access Denied");
        errorBody.put("message", "You do not have permission to access this resource");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error", "Unauthorized");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
