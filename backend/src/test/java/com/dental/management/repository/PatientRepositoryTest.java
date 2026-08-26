package com.dental.management.repository;

import com.dental.management.entity.Patient;
import com.dental.management.entity.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    private Patient createTestPatient(String patientNumber) {
        return new Patient(
                patientNumber,
                "Michael Scott",
                LocalDate.of(1975, 3, 15),
                Gender.MALE,
                "+1-555-333-7777",
                "michael@dunder.com",
                "Scranton, PA"
        );
    }

    @Test
    @DisplayName("Save and find patient by patientNumber")
    void shouldSaveAndFindByPatientNumber() {
        Patient patient = createTestPatient("PAT-1001");
        Patient saved = patientRepository.save(patient);

        assertNotNull(saved.getId());
        Optional<Patient> found = patientRepository.findByPatientNumber("PAT-1001");
        assertTrue(found.isPresent());
        assertEquals("Michael Scott", found.get().getFullName());
        assertTrue(patientRepository.existsByPatientNumber("PAT-1001"));
    }

    @Test
    @DisplayName("Duplicate patientNumber throws DataIntegrityViolationException")
    void duplicatePatientNumberShouldFail() {
        Patient p1 = createTestPatient("PAT-UNIQUE");
        patientRepository.saveAndFlush(p1);

        Patient p2 = createTestPatient("PAT-UNIQUE");
        p2.setFullName("Another Person");

        assertThrows(DataIntegrityViolationException.class, () -> {
            patientRepository.saveAndFlush(p2);
        });
    }
}
