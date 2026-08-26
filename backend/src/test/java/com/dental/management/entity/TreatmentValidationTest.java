package com.dental.management.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid treatment passes validation")
    void validTreatmentShouldPassValidation() {
        Treatment treatment = new Treatment(
                "TRT-001",
                "Dental Cleaning",
                "Standard routine dental prophylaxis",
                new BigDecimal("75.00")
        );

        Set<ConstraintViolation<Treatment>> violations = validator.validate(treatment);
        assertTrue(violations.isEmpty());
        assertTrue(treatment.isActive());
        assertNotNull(treatment.getCreatedAt());
    }

    @Test
    @DisplayName("Zero cost treatment is valid")
    void zeroCostTreatmentShouldPass() {
        Treatment treatment = new Treatment(
                "TRT-FREE",
                "Initial Consultation",
                "Complimentary dental assessment",
                BigDecimal.ZERO
        );

        Set<ConstraintViolation<Treatment>> violations = validator.validate(treatment);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Negative cost fails validation")
    void negativeCostShouldFail() {
        Treatment treatment = new Treatment(
                "TRT-002",
                "Root Canal",
                "Endodontic therapy",
                new BigDecimal("-50.00")
        );

        Set<ConstraintViolation<Treatment>> violations = validator.validate(treatment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cost")));
    }

    @Test
    @DisplayName("Blank treatmentCode fails validation")
    void blankTreatmentCodeShouldFail() {
        Treatment treatment = new Treatment(
                " ",
                "Fillings",
                "Composite fillings",
                new BigDecimal("120.00")
        );

        Set<ConstraintViolation<Treatment>> violations = validator.validate(treatment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("treatmentCode")));
    }
}
