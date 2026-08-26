package com.dental.management.repository;

import com.dental.management.entity.Treatment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TreatmentRepositoryTest {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Test
    @DisplayName("Save and find treatment by treatmentCode with BigDecimal cost")
    void shouldSaveAndFindByTreatmentCode() {
        Treatment treatment = new Treatment(
                "TRT-CROWN",
                "Porcelain Crown",
                "Full porcelain dental crown restoration",
                new BigDecimal("850.50")
        );

        Treatment saved = treatmentRepository.save(treatment);
        assertNotNull(saved.getId());

        Optional<Treatment> found = treatmentRepository.findByTreatmentCode("TRT-CROWN");
        assertTrue(found.isPresent());
        assertEquals("Porcelain Crown", found.get().getTreatmentName());
        assertEquals(0, new BigDecimal("850.50").compareTo(found.get().getCost()));
        assertTrue(treatmentRepository.existsByTreatmentCode("TRT-CROWN"));
    }

    @Test
    @DisplayName("Duplicate treatmentCode throws DataIntegrityViolationException")
    void duplicateTreatmentCodeShouldFail() {
        Treatment t1 = new Treatment("TRT-DUP", "Treatment A", null, new BigDecimal("100.00"));
        treatmentRepository.saveAndFlush(t1);

        Treatment t2 = new Treatment("TRT-DUP", "Treatment B", null, new BigDecimal("150.00"));
        assertThrows(DataIntegrityViolationException.class, () -> {
            treatmentRepository.saveAndFlush(t2);
        });
    }
}
