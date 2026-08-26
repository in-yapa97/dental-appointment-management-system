package com.dental.management.dto;

import java.time.Instant;

/**
 * DTO representing system health and application metadata.
 */
public class HealthStatusResponse {

    private String status;
    private String service;
    private Instant timestamp;
    private String version;

    public HealthStatusResponse() {
    }

    public HealthStatusResponse(String status, String service, Instant timestamp, String version) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
