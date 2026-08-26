package com.dental.management.service;

import com.dental.management.dto.HealthStatusResponse;
import com.dental.management.service.impl.HealthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthServiceTest {

    @Test
    @DisplayName("HealthService should return UP status with application metadata")
    void shouldReturnValidHealthStatus() {
        HealthService service = new HealthServiceImpl("Dental API", "0.0.1-SNAPSHOT");
        HealthStatusResponse response = service.getHealthStatus();

        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertEquals("Dental API", response.getService());
        assertEquals("0.0.1-SNAPSHOT", response.getVersion());
        assertNotNull(response.getTimestamp());
    }
}
