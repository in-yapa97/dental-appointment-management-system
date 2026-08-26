package com.dental.management.dto;

import com.dental.management.entity.Bill;
import com.dental.management.entity.enums.BillStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO representing an official receipt for a completed or billed payment.
 */
public class ReceiptResponse {

    private String receiptNumber;
    private String billNumber;
    private String appointmentNumber;
    private Long appointmentId;

    // Patient Information
    private String patientName;
    private String patientNumber;
    private String patientPhone;
    private String patientEmail;

    // Attending Dentist
    private String dentistName;
    private String dentistSpecialization;

    // Treatment
    private String treatmentName;
    private String treatmentCode;

    // Financial Breakdown
    private BigDecimal consultationFee;
    private BigDecimal treatmentAmount;
    private BigDecimal totalAmount;

    // Payment details
    private BillStatus paymentStatus;
    private LocalDate paymentDate;
    private Instant issuedAt;

    // Clinic details
    private String clinicName = "DentalCare Clinic & Implant Center";
    private String clinicAddress = "100 Health Avenue, Suite 400, Medical District";
    private String clinicContact = "+1-555-DENTAL / clinic@dentalcare.com";

    public ReceiptResponse() {
    }

    public static ReceiptResponse fromBill(Bill bill) {
        if (bill == null) {
            return null;
        }

        ReceiptResponse res = new ReceiptResponse();
        res.setReceiptNumber("REC-" + bill.getBillNumber().replace("BIL-", ""));
        res.setBillNumber(bill.getBillNumber());
        res.setConsultationFee(bill.getConsultationFee());
        res.setTreatmentAmount(bill.getTreatmentAmount());
        res.setTotalAmount(bill.getTotalAmount());
        res.setPaymentStatus(bill.getStatus());
        res.setPaymentDate(bill.getBillDate());
        res.setIssuedAt(Instant.now());

        if (bill.getAppointment() != null) {
            res.setAppointmentId(bill.getAppointment().getId());
            res.setAppointmentNumber(bill.getAppointment().getAppointmentNumber());

            if (bill.getAppointment().getPatient() != null) {
                res.setPatientName(bill.getAppointment().getPatient().getFullName());
                res.setPatientNumber(bill.getAppointment().getPatient().getPatientNumber());
                res.setPatientPhone(bill.getAppointment().getPatient().getPhone());
                res.setPatientEmail(bill.getAppointment().getPatient().getEmail());
            }

            if (bill.getAppointment().getDentist() != null) {
                res.setDentistName(bill.getAppointment().getDentist().getFullName());
                res.setDentistSpecialization(bill.getAppointment().getDentist().getSpecialization());
            }

            if (bill.getAppointment().getTreatment() != null) {
                res.setTreatmentName(bill.getAppointment().getTreatment().getTreatmentName());
                res.setTreatmentCode(bill.getAppointment().getTreatment().getTreatmentCode());
            }
        }

        return res;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public String getDentistSpecialization() {
        return dentistSpecialization;
    }

    public void setDentistSpecialization(String dentistSpecialization) {
        this.dentistSpecialization = dentistSpecialization;
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

    public BillStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(BillStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public String getClinicContact() {
        return clinicContact;
    }

    public void setClinicContact(String clinicContact) {
        this.clinicContact = clinicContact;
    }
}
