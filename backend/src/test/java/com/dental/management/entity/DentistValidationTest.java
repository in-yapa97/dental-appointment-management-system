package com.dental.management.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DentistValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Dentist createValidDentist() {
        return new Dentist(
                "DEN-001",
                "Dr. Robert Miller",
                "Orthodontics",
                "+1-555-987-6543",
                "robert.miller@clinic.com"
        );
    }

    @Test
    @DisplayName("Valid dentist passes validation")
    void validDentistShouldPassValidation() {
        Dentist dentist = createValidDentist();
        Set<ConstraintViolation<Dentist>> violations = validator.validate(dentist);
        assertTrue(violations.isEmpty());
        assertTrue(dentist.isActive());
        assertNotNull(dentist.getCreatedAt());
    }

    @Test
    @DisplayName("Blank dentistNumber fails validation")
    void blankDentistNumberShouldFail() {
        Dentist dentist = createValidDentist();
        dentist.setDentistNumber("");
        Set<ConstraintViolation<Dentist>> violations = validator.validate(dentist);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dentistNumber")));
    }

    @Test
    @DisplayName("Blank specialization fails validation")
    void blankSpecializationShouldFail() {
        Dentist dentist = createValidDentist();
        dentist.setSpecialization("   ");
        Set<ConstraintViolation<Dentist>> violations = validator.validate(dentist);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("specialization")));
    }
}
