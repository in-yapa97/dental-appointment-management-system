package com.dental.management.entity;

import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.entity.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Patient createSamplePatient() {
        return new Patient("PAT-001", "Jane Doe", LocalDate.of(1995, 3, 10),
                Gender.FEMALE, "+1-555-222-3333", "jane@example.com", "456 Oak St");
    }

    private Dentist createSampleDentist() {
        return new Dentist("DEN-001", "Dr. Davis", "General Dentistry",
                "+1-555-444-5555", "davis@clinic.com");
    }

    private Treatment createSampleTreatment() {
        return new Treatment("TRT-001", "Teeth Whitening", "Cosmetic bleaching", new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Valid appointment passes validation")
    void validAppointmentShouldPassValidation() {
        Appointment appointment = new Appointment(
                "APP-001",
                createSamplePatient(),
                createSampleDentist(),
                createSampleTreatment(),
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 30),
                AppointmentStatus.SCHEDULED,
                "First routine visit"
        );

        Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
        assertTrue(violations.isEmpty());
        assertNotNull(appointment.getCreatedAt());
    }

    @Test
    @DisplayName("Null patient fails validation")
    void nullPatientShouldFail() {
        Appointment appointment = new Appointment(
                "APP-001",
                null,
                createSampleDentist(),
                createSampleTreatment(),
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 30),
                AppointmentStatus.SCHEDULED,
                null
        );

        Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("patient")));
    }

    @Test
    @DisplayName("Null dentist fails validation")
    void nullDentistShouldFail() {
        Appointment appointment = new Appointment(
                "APP-001",
                createSamplePatient(),
                null,
                createSampleTreatment(),
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 30),
                AppointmentStatus.SCHEDULED,
                null
        );

        Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dentist")));
    }

    @Test
    @DisplayName("Null treatment fails validation")
    void nullTreatmentShouldFail() {
        Appointment appointment = new Appointment(
                "APP-001",
                createSamplePatient(),
                createSampleDentist(),
                null,
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 30),
                AppointmentStatus.SCHEDULED,
                null
        );

        Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("treatment")));
    }

    @Test
    @DisplayName("Null appointment date fails validation")
    void nullAppointmentDateShouldFail() {
        Appointment appointment = new Appointment(
                "APP-001",
                createSamplePatient(),
                createSampleDentist(),
                createSampleTreatment(),
                null,
                LocalTime.of(10, 30),
                AppointmentStatus.SCHEDULED,
                null
        );

        Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate")));
    }
}
