package com.dental.management.entity;

import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.entity.enums.BillStatus;
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

class BillValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Appointment createSampleAppointment() {
        Patient patient = new Patient("PAT-001", "Jane Doe", LocalDate.of(1995, 3, 10),
                Gender.FEMALE, "+1-555-222-3333", "jane@example.com", "456 Oak St");
        Dentist dentist = new Dentist("DEN-001", "Dr. Davis", "General Dentistry",
                "+1-555-444-5555", "davis@clinic.com");
        Treatment treatment = new Treatment("TRT-001", "Teeth Whitening", "Cosmetic bleaching", new BigDecimal("200.00"));
        return new Appointment("APP-001", patient, dentist, treatment,
                LocalDate.of(2026, 9, 1), LocalTime.of(10, 30), AppointmentStatus.CONFIRMED, null);
    }

    @Test
    @DisplayName("Valid bill passes validation")
    void validBillShouldPassValidation() {
        Bill bill = new Bill(
                "BILL-001",
                createSampleAppointment(),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("250.00"),
                LocalDate.of(2026, 9, 1),
                BillStatus.PENDING
        );

        Set<ConstraintViolation<Bill>> violations = validator.validate(bill);
        assertTrue(violations.isEmpty());
        assertNotNull(bill.getCreatedAt());
    }

    @Test
    @DisplayName("Negative total amount fails validation")
    void negativeTotalAmountShouldFail() {
        Bill bill = new Bill(
                "BILL-002",
                createSampleAppointment(),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("-10.00"),
                LocalDate.of(2026, 9, 1),
                BillStatus.PENDING
        );

        Set<ConstraintViolation<Bill>> violations = validator.validate(bill);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("totalAmount")));
    }

    @Test
    @DisplayName("Negative consultation fee fails validation")
    void negativeConsultationFeeShouldFail() {
        Bill bill = new Bill(
                "BILL-003",
                createSampleAppointment(),
                new BigDecimal("-25.00"),
                new BigDecimal("200.00"),
                new BigDecimal("175.00"),
                LocalDate.of(2026, 9, 1),
                BillStatus.PENDING
        );

        Set<ConstraintViolation<Bill>> violations = validator.validate(bill);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("consultationFee")));
    }

    @Test
    @DisplayName("Null appointment fails validation")
    void nullAppointmentShouldFail() {
        Bill bill = new Bill(
                "BILL-004",
                null,
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("250.00"),
                LocalDate.of(2026, 9, 1),
                BillStatus.PENDING
        );

        Set<ConstraintViolation<Bill>> violations = validator.validate(bill);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("appointment")));
    }
}
