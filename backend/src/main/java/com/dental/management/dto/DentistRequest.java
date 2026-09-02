package com.dental.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating and updating Dentist records.
 */
public class DentistRequest {

    @NotBlank(message = "Dentist number is required")
    @Size(max = 30, message = "Dentist number cannot exceed 30 characters")
    private String dentistNumber;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    private String specialization;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+0-9\\-\\s()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email address format")
    private String email;

    private boolean active = true;

    public DentistRequest() {
    }

    public DentistRequest(String dentistNumber, String fullName, String specialization, String phone, String email, boolean active) {
        this.dentistNumber = dentistNumber;
        this.fullName = fullName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.active = active;
    }

    public String getDentistNumber() {
        return dentistNumber;
    }

    public void setDentistNumber(String dentistNumber) {
        this.dentistNumber = dentistNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
