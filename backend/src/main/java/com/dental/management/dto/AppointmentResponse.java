package com.dental.management.dto;

import com.dental.management.entity.Appointment;
import com.dental.management.entity.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response payload DTO returning complete appointment details with associated
 * patient, dentist, and treatment metadata.
 */
public class AppointmentResponse {

    private Long id;
    private String appointmentNumber;
    private Long patientId;
    private String patientName;
    private String patientNumber;
    private Long dentistId;
    private String dentistName;
    private String dentistNumber;
    private Long treatmentId;
    private String treatmentName;
    private String treatmentCode;
    private BigDecimal treatmentCost;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String notes;
    private Instant createdAt;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, String appointmentNumber, Long patientId, String patientName,
                               String patientNumber, Long dentistId, String dentistName, String dentistNumber,
                               Long treatmentId, String treatmentName, String treatmentCode, BigDecimal treatmentCost,
                               LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status,
                               String notes, Instant createdAt) {
        this.id = id;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientNumber = patientNumber;
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.dentistNumber = dentistNumber;
        this.treatmentId = treatmentId;
        this.treatmentName = treatmentName;
        this.treatmentCode = treatmentCode;
        this.treatmentCost = treatmentCost;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static AppointmentResponse fromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getAppointmentNumber(),
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getPatient() != null ? appointment.getPatient().getFullName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getPatientNumber() : null,
                appointment.getDentist() != null ? appointment.getDentist().getId() : null,
                appointment.getDentist() != null ? appointment.getDentist().getFullName() : null,
                appointment.getDentist() != null ? appointment.getDentist().getDentistNumber() : null,
                appointment.getTreatment() != null ? appointment.getTreatment().getId() : null,
                appointment.getTreatment() != null ? appointment.getTreatment().getTreatmentName() : null,
                appointment.getTreatment() != null ? appointment.getTreatment().getTreatmentCode() : null,
                appointment.getTreatment() != null ? appointment.getTreatment().getCost() : null,
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDentistNumber() {
        return dentistNumber;
    }

    public void setDentistNumber(String dentistNumber) {
        this.dentistNumber = dentistNumber;
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

    public BigDecimal getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(BigDecimal treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
