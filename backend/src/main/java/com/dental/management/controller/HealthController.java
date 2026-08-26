package com.dental.management.controller;

import com.dental.management.dto.HealthStatusResponse;
import com.dental.management.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for verifying backend system health and operational status.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * GET /api/v1/health
     * Returns application health status and metadata.
     */
    @GetMapping
    public ResponseEntity<HealthStatusResponse> getHealthStatus() {
        HealthStatusResponse response = healthService.getHealthStatus();
        return ResponseEntity.ok(response);
    }
}
