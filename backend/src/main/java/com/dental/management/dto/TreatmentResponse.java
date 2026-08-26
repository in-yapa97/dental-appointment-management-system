package com.dental.management.dto;

import com.dental.management.entity.Treatment;

import java.math.BigDecimal;

/**
 * Response DTO representing dental treatment / procedure details for selection and display.
 */
public class TreatmentResponse {

    private Long id;
    private String treatmentCode;
    private String treatmentName;
    private String description;
    private BigDecimal cost;
    private boolean active;

    public TreatmentResponse() {
    }

    public TreatmentResponse(Long id, String treatmentCode, String treatmentName,
                             String description, BigDecimal cost, boolean active) {
        this.id = id;
        this.treatmentCode = treatmentCode;
        this.treatmentName = treatmentName;
        this.description = description;
        this.cost = cost;
        this.active = active;
    }

    public static TreatmentResponse fromEntity(Treatment treatment) {
        if (treatment == null) {
            return null;
        }
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getTreatmentCode(),
                treatment.getTreatmentName(),
                treatment.getDescription(),
                treatment.getCost(),
                treatment.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTreatmentCode() {
        return treatmentCode;
    }

    public void setTreatmentCode(String treatmentCode) {
        this.treatmentCode = treatmentCode;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
