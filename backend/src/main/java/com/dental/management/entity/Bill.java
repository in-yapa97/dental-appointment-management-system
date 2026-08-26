package com.dental.management.entity;

import com.dental.management.entity.enums.BillStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity representing an invoice/bill associated with an appointment.
 */
@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bill number is required")
    @Column(name = "bill_number", nullable = false, unique = true, length = 30)
    private String billNumber;

    @NotNull(message = "Appointment is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @NotNull(message = "Consultation fee is required")
    @PositiveOrZero(message = "Consultation fee must not be negative")
    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @NotNull(message = "Treatment amount is required")
    @PositiveOrZero(message = "Treatment amount must not be negative")
    @Column(name = "treatment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal treatmentAmount;

    @NotNull(message = "Total amount is required")
    @PositiveOrZero(message = "Total amount must not be negative")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Bill date is required")
    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @NotNull(message = "Bill status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Bill() {
    }

    public Bill(String billNumber, Appointment appointment, BigDecimal consultationFee,
                BigDecimal treatmentAmount, BigDecimal totalAmount, LocalDate billDate, BillStatus status) {
        this.billNumber = billNumber;
        this.appointment = appointment;
        this.consultationFee = consultationFee;
        this.treatmentAmount = treatmentAmount;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
        this.status = status != null ? status : BillStatus.PENDING;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = BillStatus.PENDING;
        }
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

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
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
