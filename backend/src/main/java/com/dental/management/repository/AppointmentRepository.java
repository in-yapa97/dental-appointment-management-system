package com.dental.management.repository;

import com.dental.management.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Appointment entity operations.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find an appointment by its unique appointment number.
     *
     * @param appointmentNumber the appointment identifier
     * @return Optional containing the Appointment if found, or empty
     */
    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    /**
     * Check if an appointment exists with the given appointment number.
     *
     * @param appointmentNumber the appointment identifier to check
     * @return true if a record exists, false otherwise
     */
    boolean existsByAppointmentNumber(String appointmentNumber);

    /**
     * Find all appointments for a specific patient.
     *
     * @param patientId the ID of the patient
     * @return List of appointments for that patient
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Find all appointments for a specific dentist.
     *
     * @param dentistId the ID of the dentist
     * @return List of appointments for that dentist
     */
    List<Appointment> findByDentistId(Long dentistId);
}
