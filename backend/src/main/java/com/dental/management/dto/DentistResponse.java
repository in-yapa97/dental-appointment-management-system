package com.dental.management.dto;

import com.dental.management.entity.Dentist;

/**
 * Response DTO representing dentist summary details for selection and display.
 */
public class DentistResponse {

    private Long id;
    private String dentistNumber;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;
    private boolean active;

    public DentistResponse() {
    }

    public DentistResponse(Long id, String dentistNumber, String fullName, String specialization,
                           String phone, String email, boolean active) {
        this.id = id;
        this.dentistNumber = dentistNumber;
        this.fullName = fullName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.active = active;
    }

    public static DentistResponse fromEntity(Dentist dentist) {
        if (dentist == null) {
            return null;
        }
        return new DentistResponse(
                dentist.getId(),
                dentist.getDentistNumber(),
                dentist.getFullName(),
                dentist.getSpecialization(),
                dentist.getPhone(),
                dentist.getEmail(),
                dentist.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
