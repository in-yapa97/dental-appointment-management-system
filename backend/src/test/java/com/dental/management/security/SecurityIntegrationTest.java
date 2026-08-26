package com.dental.management.security;

import com.dental.management.dto.RegisterRequest;
import com.dental.management.repository.UserRepository;
import com.dental.management.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Health endpoint /api/v1/health is public and returns 200 without token")
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Registration endpoint /api/v1/auth/register is public and returns 201 without token")
    void registrationEndpointIsPublic() throws Exception {
        RegisterRequest request = new RegisterRequest("sec_integration_user", "password123", "Sec User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("sec_integration_user"));
    }

    @Test
    @DisplayName("Protected endpoint /api/v1/auth/me returns 401 Unauthorized without token")
    void protectedEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Protected endpoint /api/v1/auth/me returns 200 with valid JWT token")
    void protectedEndpointAccessibleWithValidToken() throws Exception {
        RegisterRequest request = new RegisterRequest("token_user", "password123", "Token User");
        authService.register(request);

        String token = jwtUtils.generateToken("token_user", "STAFF");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("token_user"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @DisplayName("Protected endpoint returns 401 Unauthorized with invalid JWT token")
    void protectedEndpointRejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid.tampered.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
