package com.dental.management.dto;

import com.dental.management.entity.enums.BillStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or updating a bill record.
 */
public class BillRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @PositiveOrZero(message = "Consultation fee must not be negative")
    private BigDecimal consultationFee;

    @PositiveOrZero(message = "Treatment amount must not be negative")
    private BigDecimal treatmentAmount;

    private LocalDate billDate;

    private BillStatus status;

    public BillRequest() {
    }

    public BillRequest(Long appointmentId, BigDecimal consultationFee, BigDecimal treatmentAmount,
                       LocalDate billDate, BillStatus status) {
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.treatmentAmount = treatmentAmount;
        this.billDate = billDate;
        this.status = status;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public BigDecimal getTreatmentAmount() {
        return treatmentAmount;
    }

    public void setTreatmentAmount(BigDecimal treatmentAmount) {
        this.treatmentAmount = treatmentAmount;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }
}
