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

    @Test
    @DisplayName("isDentistBooked accurately detects schedule conflicts excluding cancelled appointments")
    void testIsDentistBooked() {
        Patient patient = entityManager.persist(new Patient("PAT-700", "P700", LocalDate.of(1980, 1, 1),
                Gender.FEMALE, "+1-555-123-0000", null, null));
        Dentist dentist = entityManager.persist(new Dentist("DEN-700", "D700", "Orthodontics",
                "+1-555-123-1111", null));
        Treatment treatment = entityManager.persist(new Treatment("TRT-700", "T700", null, new BigDecimal("80.00")));

        LocalDate date = LocalDate.of(2026, 10, 5);
        LocalTime time = LocalTime.of(11, 0);

        // Before booking: should be free
        assertFalse(appointmentRepository.isDentistBooked(dentist.getId(), date, time, AppointmentStatus.CANCELLED, null));

        // Book slot
        Appointment a1 = appointmentRepository.saveAndFlush(new Appointment("APP-701", patient, dentist, treatment,
                date, time, AppointmentStatus.SCHEDULED, null));

        // Now should be booked
        assertTrue(appointmentRepository.isDentistBooked(dentist.getId(), date, time, AppointmentStatus.CANCELLED, null));

        // When excluding current appointment id (for updates): should be free
        assertFalse(appointmentRepository.isDentistBooked(dentist.getId(), date, time, AppointmentStatus.CANCELLED, a1.getId()));

        // If cancelled: should be free
        a1.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(a1);
        assertFalse(appointmentRepository.isDentistBooked(dentist.getId(), date, time, AppointmentStatus.CANCELLED, null));
    }

    @Test
    @DisplayName("findWithFilters returns appointments matching criteria")
    void testFindWithFilters() {
        Patient patient = entityManager.persist(new Patient("PAT-800", "P800", LocalDate.of(1980, 1, 1),
                Gender.MALE, "+1-555-123-0000", null, null));
        Dentist dentist = entityManager.persist(new Dentist("DEN-800", "D800", "Surgery",
                "+1-555-123-1111", null));
        Treatment treatment = entityManager.persist(new Treatment("TRT-800", "T800", null, new BigDecimal("120.00")));

        Appointment a1 = appointmentRepository.saveAndFlush(new Appointment("APP-801", patient, dentist, treatment,
                LocalDate.of(2026, 11, 1), LocalTime.of(9, 30), AppointmentStatus.SCHEDULED, null));

        List<Appointment> filtered = appointmentRepository.findWithFilters(patient.getId(), dentist.getId(), LocalDate.of(2026, 11, 1), AppointmentStatus.SCHEDULED);
        assertEquals(1, filtered.size());
        assertEquals("APP-801", filtered.get(0).getAppointmentNumber());

        List<Appointment> noMatch = appointmentRepository.findWithFilters(patient.getId(), dentist.getId(), LocalDate.of(2026, 11, 2), AppointmentStatus.SCHEDULED);
        assertTrue(noMatch.isEmpty());
    }
}
