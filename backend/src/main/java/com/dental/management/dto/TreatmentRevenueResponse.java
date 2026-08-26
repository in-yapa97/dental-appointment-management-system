package com.dental.management.dto;

import java.math.BigDecimal;

/**
 * Response DTO representing revenue generated grouped by treatment procedure.
 */
public class TreatmentRevenueResponse {

    private Long treatmentId;
    private String treatmentCode;
    private String treatmentName;
    private long billCount;
    private BigDecimal totalRevenue;

    public TreatmentRevenueResponse() {
    }

    public TreatmentRevenueResponse(Long treatmentId, String treatmentCode, String treatmentName,
                                    long billCount, BigDecimal totalRevenue) {
        this.treatmentId = treatmentId;
        this.treatmentCode = treatmentCode;
        this.treatmentName = treatmentName;
        this.billCount = billCount;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public Long getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(Long treatmentId) {
        this.treatmentId = treatmentId;
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

    public long getBillCount() {
        return billCount;
    }

    public void setBillCount(long billCount) {
        this.billCount = billCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
