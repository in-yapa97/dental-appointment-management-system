package com.dental.management.service;

import com.dental.management.dto.HealthStatusResponse;

/**
 * Service interface for retrieving system health status.
 */
public interface HealthService {

    /**
     * Retrieve the current health status of the application.
     *
     * @return HealthStatusResponse containing system status, timestamp, and version
     */
    HealthStatusResponse getHealthStatus();
}
