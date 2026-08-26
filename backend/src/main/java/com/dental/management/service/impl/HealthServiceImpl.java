package com.dental.management.service.impl;

import com.dental.management.dto.HealthStatusResponse;
import com.dental.management.service.HealthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation of HealthService.
 */
@Service
public class HealthServiceImpl implements HealthService {

    private final String appName;
    private final String appVersion;

    public HealthServiceImpl(
            @Value("${info.app.name:Dental Appointment and Patient Management System API}") String appName,
            @Value("${info.app.version:0.0.1-SNAPSHOT}") String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    @Override
    public HealthStatusResponse getHealthStatus() {
        return new HealthStatusResponse("UP", appName, Instant.now(), appVersion);
    }
}
