package com.dental.management.controller;

import com.dental.management.dto.HealthStatusResponse;
import com.dental.management.service.HealthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    @Test
    @DisplayName("GET /api/v1/health should return UP status and service info")
    void shouldReturnHealthStatus() throws Exception {
        HealthStatusResponse mockResponse = new HealthStatusResponse(
                "UP",
                "Dental Appointment and Patient Management System API",
                Instant.parse("2026-08-26T12:00:00Z"),
                "0.0.1-SNAPSHOT"
        );

        when(healthService.getHealthStatus()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Dental Appointment and Patient Management System API"))
                .andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.timestamp").value("2026-08-26T12:00:00Z"));
    }
}
