package com.dental.management.repository;

import com.dental.management.entity.Appointment;
import com.dental.management.entity.Dentist;
import com.dental.management.entity.Patient;
import com.dental.management.entity.Treatment;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.entity.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    @DisplayName("Save appointment with relationships and query by patient and dentist")
    void shouldSaveAppointmentWithRelationships() {
        Patient patient = new Patient("PAT-500", "Tom Hanks", LocalDate.of(1960, 7, 9),
                Gender.MALE, "+1-555-111-9999", "tom@example.com", "Los Angeles, CA");
        entityManager.persist(patient);

        Dentist dentist = new Dentist("DEN-500", "Dr. Wilson", "Endodontics",
                "+1-555-888-2222", "wilson@clinic.com");
        entityManager.persist(dentist);

        Treatment treatment = new Treatment("TRT-500", "Extraction", "Simple tooth extraction",
                new BigDecimal("150.00"));
        entityManager.persist(treatment);

        Appointment appointment = new Appointment(
                "APP-2026-001",
                patient,
                dentist,
                treatment,
                LocalDate.of(2026, 9, 15),
                LocalTime.of(14, 0),
                AppointmentStatus.SCHEDULED,
                "Patient reported mild discomfort"
        );

        Appointment saved = appointmentRepository.save(appointment);
        assertNotNull(saved.getId());

        Optional<Appointment> found = appointmentRepository.findByAppointmentNumber("APP-2026-001");
        assertTrue(found.isPresent());
        assertEquals("Tom Hanks", found.get().getPatient().getFullName());
        assertEquals("Dr. Wilson", found.get().getDentist().getFullName());
        assertEquals("Extraction", found.get().getTreatment().getTreatmentName());
        assertEquals(AppointmentStatus.SCHEDULED, found.get().getStatus());

        List<Appointment> byPatient = appointmentRepository.findByPatientId(patient.getId());
        assertEquals(1, byPatient.size());

        List<Appointment> byDentist = appointmentRepository.findByDentistId(dentist.getId());
        assertEquals(1, byDentist.size());
    }

    @Test
    @DisplayName("Duplicate appointmentNumber throws DataIntegrityViolationException")
    void duplicateAppointmentNumberShouldFail() {
        Patient patient = entityManager.persist(new Patient("PAT-600", "P1", LocalDate.of(1980, 1, 1),
                Gender.FEMALE, "+1-555-123-0000", null, null));
        Dentist dentist = entityManager.persist(new Dentist("DEN-600", "D1", "General",
                "+1-555-123-1111", null));
        Treatment treatment = entityManager.persist(new Treatment("TRT-600", "T1", null, new BigDecimal("50.00")));

        Appointment a1 = new Appointment("APP-DUP", patient, dentist, treatment,
                LocalDate.of(2026, 9, 20), LocalTime.of(9, 0), AppointmentStatus.SCHEDULED, null);
        appointmentRepository.saveAndFlush(a1);

        Appointment a2 = new Appointment("APP-DUP", patient, dentist, treatment,
                LocalDate.of(2026, 9, 21), LocalTime.of(10, 0), AppointmentStatus.SCHEDULED, null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            appointmentRepository.saveAndFlush(a2);
        });
    }
}
