package com.dental.management.controller;

import com.dental.management.config.SecurityConfig;
import com.dental.management.dto.AuthResponse;
import com.dental.management.dto.LoginRequest;
import com.dental.management.dto.RegisterRequest;
import com.dental.management.dto.UserResponse;
import com.dental.management.entity.enums.UserRole;
import com.dental.management.exception.DuplicateUsernameException;
import com.dental.management.exception.InactiveUserException;
import com.dental.management.exception.InvalidCredentialsException;
import com.dental.management.security.JwtAuthenticationEntryPoint;
import com.dental.management.security.JwtAuthenticationFilter;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class, JwtUtils.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/register - 201 Created on valid request")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("janedoe", "validPassword123", "Jane Doe");
        UserResponse response = new UserResponse(1L, "janedoe", "Jane Doe", UserRole.STAFF, true, Instant.now());

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - 400 Bad Request on validation failure")
    void shouldFailValidationOnShortPassword() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("jd", "123", "");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - 409 Conflict on duplicate username")
    void shouldReturnConflictOnDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest("existinguser", "password123", "Existing User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateUsernameException("Username 'existinguser' is already taken"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username 'existinguser' is already taken"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 200 OK on valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("janedoe", "correctPassword");
        UserResponse userResponse = new UserResponse(1L, "janedoe", "Jane Doe", UserRole.STAFF, true, Instant.now());
        AuthResponse authResponse = new AuthResponse("mock.jwt.token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("janedoe"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 401 Unauthorized on invalid credentials")
    void shouldReturnUnauthorizedOnInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("janedoe", "wrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 403 Forbidden on inactive user")
    void shouldReturnForbiddenOnInactiveUser() throws Exception {
        LoginRequest request = new LoginRequest("inactiveuser", "password123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InactiveUserException("User account is inactive. Please contact your system administrator."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("User account is inactive. Please contact your system administrator."));
    }

    @Test
    @WithMockUser(username = "authuser", roles = {"STAFF"})
    @DisplayName("GET /api/v1/auth/me - 200 OK for authenticated user")
    void shouldReturnCurrentUserWhenAuthenticated() throws Exception {
        UserResponse userResponse = new UserResponse(5L, "authuser", "Authenticated User", UserRole.STAFF, true, Instant.now());

        when(authService.getCurrentUser(eq("authuser"))).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.username").value("authuser"))
                .andExpect(jsonPath("$.fullName").value("Authenticated User"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - 401 Unauthorized for unauthenticated request")
    void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - 200 OK")
    void shouldReturnLogoutMessage() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}
