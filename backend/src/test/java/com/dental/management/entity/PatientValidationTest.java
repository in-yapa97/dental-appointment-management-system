package com.dental.management.entity;

import com.dental.management.entity.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PatientValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Patient createValidPatient() {
        return new Patient(
                "PAT-001",
                "Alice Johnson",
                LocalDate.of(1990, 5, 15),
                Gender.FEMALE,
                "+1-555-123-4567",
                "alice.johnson@example.com",
                "123 Maple Street, Cityville"
        );
    }

    @Test
    @DisplayName("Valid patient passes validation")
    void validPatientShouldPassValidation() {
        Patient patient = createValidPatient();
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);
        assertTrue(violations.isEmpty(), "Valid patient should have zero violations");
        assertNotNull(patient.getCreatedAt());
    }

    @Test
    @DisplayName("Blank patientNumber fails validation")
    void blankPatientNumberShouldFail() {
        Patient patient = createValidPatient();
        patient.setPatientNumber("  ");
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("patientNumber")));
    }

    @Test
    @DisplayName("Future date of birth fails validation")
    void futureDateOfBirthShouldFail() {
        Patient patient = createValidPatient();
        patient.setDateOfBirth(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
    }

    @Test
    @DisplayName("Invalid email format fails validation")
    void invalidEmailShouldFail() {
        Patient patient = createValidPatient();
        patient.setEmail("not-a-valid-email");
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Invalid phone format fails validation")
    void invalidPhoneShouldFail() {
        Patient patient = createValidPatient();
        patient.setPhone("invalid");
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }
}
