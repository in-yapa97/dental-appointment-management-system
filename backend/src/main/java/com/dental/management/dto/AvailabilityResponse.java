package com.dental.management.dto;

/**
 * Response DTO for checking dentist availability on a specific date and time.
 */
public class AvailabilityResponse {

    private boolean available;
    private String reason;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(boolean available, String reason) {
        this.available = available;
        this.reason = reason;
    }

    public static AvailabilityResponse available() {
        return new AvailabilityResponse(true, "Dentist is available for the selected slot");
    }

    public static AvailabilityResponse unavailable(String reason) {
        return new AvailabilityResponse(false, reason);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
