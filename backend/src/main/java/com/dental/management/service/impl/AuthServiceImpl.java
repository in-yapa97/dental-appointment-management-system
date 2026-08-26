package com.dental.management.service.impl;

import com.dental.management.dto.AuthResponse;
import com.dental.management.dto.LoginRequest;
import com.dental.management.dto.RegisterRequest;
import com.dental.management.dto.UserResponse;
import com.dental.management.entity.User;
import com.dental.management.entity.enums.UserRole;
import com.dental.management.exception.DuplicateUsernameException;
import com.dental.management.exception.InactiveUserException;
import com.dental.management.exception.InvalidCredentialsException;
import com.dental.management.repository.UserRepository;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService delivering business logic for user registration,
 * authentication, password hashing, and user profile queries.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Username '" + request.getUsername() + "' is already taken");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getUsername(),
                hashedPassword,
                request.getFullName(),
                UserRole.STAFF
        );
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new InactiveUserException("User account is inactive. Please contact your system administrator.");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + username));
        return UserResponse.fromEntity(user);
    }
}
