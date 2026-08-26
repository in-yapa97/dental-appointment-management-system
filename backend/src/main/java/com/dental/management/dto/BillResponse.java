package com.dental.management.dto;

import com.dental.management.entity.Bill;
import com.dental.management.entity.enums.BillStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO representing bill details with associated appointment, patient,
 * dentist, and treatment metadata.
 */
public class BillResponse {

    private Long id;
    private String billNumber;
    private Long appointmentId;
    private String appointmentNumber;
    private Long patientId;
    private String patientName;
    private String patientNumber;
    private Long dentistId;
    private String dentistName;
    private Long treatmentId;
    private String treatmentName;
    private String treatmentCode;
    private BigDecimal consultationFee;
    private BigDecimal treatmentAmount;
    private BigDecimal totalAmount;
    private LocalDate billDate;
    private BillStatus status;
    private Instant createdAt;

    public BillResponse() {
    }

    public BillResponse(Long id, String billNumber, Long appointmentId, String appointmentNumber,
                        Long patientId, String patientName, String patientNumber,
                        Long dentistId, String dentistName,
                        Long treatmentId, String treatmentName, String treatmentCode,
                        BigDecimal consultationFee, BigDecimal treatmentAmount, BigDecimal totalAmount,
                        LocalDate billDate, BillStatus status, Instant createdAt) {
        this.id = id;
        this.billNumber = billNumber;
        this.appointmentId = appointmentId;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientNumber = patientNumber;
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.treatmentId = treatmentId;
        this.treatmentName = treatmentName;
        this.treatmentCode = treatmentCode;
        this.consultationFee = consultationFee;
        this.treatmentAmount = treatmentAmount;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static BillResponse fromEntity(Bill bill) {
        if (bill == null) {
            return null;
        }

        Long appointmentId = null;
        String appointmentNumber = null;
        Long patientId = null;
        String patientName = null;
        String patientNumber = null;
        Long dentistId = null;
        String dentistName = null;
        Long treatmentId = null;
        String treatmentName = null;
        String treatmentCode = null;

        if (bill.getAppointment() != null) {
            appointmentId = bill.getAppointment().getId();
            appointmentNumber = bill.getAppointment().getAppointmentNumber();

            if (bill.getAppointment().getPatient() != null) {
                patientId = bill.getAppointment().getPatient().getId();
                patientName = bill.getAppointment().getPatient().getFullName();
                patientNumber = bill.getAppointment().getPatient().getPatientNumber();
            }

            if (bill.getAppointment().getDentist() != null) {
                dentistId = bill.getAppointment().getDentist().getId();
                dentistName = bill.getAppointment().getDentist().getFullName();
            }

            if (bill.getAppointment().getTreatment() != null) {
                treatmentId = bill.getAppointment().getTreatment().getId();
                treatmentName = bill.getAppointment().getTreatment().getTreatmentName();
                treatmentCode = bill.getAppointment().getTreatment().getTreatmentCode();
            }
        }

        return new BillResponse(
                bill.getId(),
                bill.getBillNumber(),
                appointmentId,
                appointmentNumber,
                patientId,
                patientName,
                patientNumber,
                dentistId,
                dentistName,
                treatmentId,
                treatmentName,
                treatmentCode,
                bill.getConsultationFee(),
                bill.getTreatmentAmount(),
                bill.getTotalAmount(),
                bill.getBillDate(),
                bill.getStatus(),
                bill.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public Long getDentistId() {
        return dentistId;
    }

    public void setDentistId(Long dentistId) {
        this.dentistId = dentistId;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public Long getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(Long treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getTreatmentCode() {
        return treatmentCode;
    }

    public void setTreatmentCode(String treatmentCode) {
        this.treatmentCode = treatmentCode;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
