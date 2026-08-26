package com.dental.management.repository;

import com.dental.management.entity.Dentist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DentistRepositoryTest {

    @Autowired
    private DentistRepository dentistRepository;

    private Dentist createTestDentist(String dentistNumber) {
        return new Dentist(
                dentistNumber,
                "Dr. Sarah Connor",
                "Periodontics",
                "+1-555-444-1111",
                "sarah.c@clinic.com"
        );
    }

    @Test
    @DisplayName("Save and find dentist by dentistNumber")
    void shouldSaveAndFindByDentistNumber() {
        Dentist dentist = createTestDentist("DEN-2001");
        Dentist saved = dentistRepository.save(dentist);

        assertNotNull(saved.getId());
        Optional<Dentist> found = dentistRepository.findByDentistNumber("DEN-2001");
        assertTrue(found.isPresent());
        assertEquals("Dr. Sarah Connor", found.get().getFullName());
        assertTrue(found.get().isActive());
        assertTrue(dentistRepository.existsByDentistNumber("DEN-2001"));
    }

    @Test
    @DisplayName("Duplicate dentistNumber throws DataIntegrityViolationException")
    void duplicateDentistNumberShouldFail() {
        Dentist d1 = createTestDentist("DEN-UNIQUE");
        dentRepositorySave(d1);

        Dentist d2 = createTestDentist("DEN-UNIQUE");
        assertThrows(DataIntegrityViolationException.class, () -> {
            dentistRepository.saveAndFlush(d2);
        });
    }

    private void dentRepositorySave(Dentist d) {
        dentistRepository.saveAndFlush(d);
    }
}
