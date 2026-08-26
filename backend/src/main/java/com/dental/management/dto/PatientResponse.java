package com.dental.management.dto;

import com.dental.management.entity.Patient;
import com.dental.management.entity.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response payload DTO containing complete patient details.
 */
public class PatientResponse {

    private Long id;
    private String patientNumber;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private Instant createdAt;

    public PatientResponse() {
    }

    public PatientResponse(Long id, String patientNumber, String fullName, LocalDate dateOfBirth,
                           Gender gender, String phone, String email, String address, Instant createdAt) {
        this.id = id;
        this.patientNumber = patientNumber;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
    }

    public static PatientResponse fromEntity(Patient patient) {
        if (patient == null) {
            return null;
        }
        return new PatientResponse(
                patient.getId(),
                patient.getPatientNumber(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
