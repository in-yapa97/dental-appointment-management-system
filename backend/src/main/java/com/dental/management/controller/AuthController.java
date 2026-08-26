package com.dental.management.controller;

import com.dental.management.dto.AuthResponse;
import com.dental.management.dto.LoginRequest;
import com.dental.management.dto.MessageResponse;
import com.dental.management.dto.RegisterRequest;
import com.dental.management.dto.UserResponse;
import com.dental.management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * REST controller for authentication, registration, logout, and current user operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user with standard staff privileges.
     *
     * @param request registration request payload
     * @return 201 CREATED with safe user profile
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user credentials and return a signed JWT token.
     *
     * @param request login credentials payload
     * @return 200 OK with JWT token and safe user profile
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve the currently authenticated user's profile.
     *
     * @param principal authenticated user principal
     * @return 200 OK with authenticated user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = authService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Stateless JWT logout. Confirms client-side token discarding.
     *
     * @return 200 OK with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
