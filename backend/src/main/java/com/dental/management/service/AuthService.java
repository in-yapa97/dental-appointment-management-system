package com.dental.management.service;

import com.dental.management.dto.AuthResponse;
import com.dental.management.dto.LoginRequest;
import com.dental.management.dto.RegisterRequest;
import com.dental.management.dto.UserResponse;

/**
 * Service interface defining authentication and user operations.
 */
public interface AuthService {

    /**
     * Register a new system user with hashed password and default STAFF role.
     *
     * @param request registration details
     * @return safe user representation
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticate user credentials and produce a JWT authentication response.
     *
     * @param request login credentials
     * @return authentication response containing JWT and safe user details
     */
    AuthResponse login(LoginRequest request);

    /**
     * Fetch user details for the currently authenticated user by username.
     *
     * @param username authenticated username
     * @return safe user representation
     */
    UserResponse getCurrentUser(String username);
}
