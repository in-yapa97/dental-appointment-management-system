package com.dental.management.entity;

import com.dental.management.entity.enums.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid user passes validation")
    void validUserShouldPassValidation() {
        User user = new User("dr_smith", "SecurePass123!", "Dr. John Smith", UserRole.DENTIST);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should produce no validation violations");
        assertTrue(user.isActive(), "User active flag should default to true");
        assertNotNull(user.getCreatedAt(), "User createdAt should be initialized");
    }

    @Test
    @DisplayName("Blank username fails validation")
    void blankUsernameShouldFail() {
        User user = new User("  ", "SecurePass123!", "Dr. John Smith", UserRole.DENTIST);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("Short username fails validation")
    void shortUsernameShouldFail() {
        User user = new User("ab", "SecurePass123!", "Dr. John Smith", UserRole.DENTIST);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("Blank password fails validation")
    void blankPasswordShouldFail() {
        User user = new User("dr_smith", "", "Dr. John Smith", UserRole.DENTIST);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("Blank fullName fails validation")
    void blankFullNameShouldFail() {
        User user = new User("dr_smith", "SecurePass123!", "", UserRole.DENTIST);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
    }

    @Test
    @DisplayName("Null role fails validation")
    void nullRoleShouldFail() {
        User user = new User("dr_smith", "SecurePass123!", "Dr. John Smith", null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }
}
