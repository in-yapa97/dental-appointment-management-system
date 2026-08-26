package com.dental.management.repository;

import com.dental.management.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Appointment entity operations.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

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

    /**
     * Check if a dentist already has an active (non-cancelled) appointment at the specified date and time,
     * optionally excluding a specific appointment (for updates).
     *
     * @param dentistId      dentist ID
     * @param date           appointment date
     * @param time           appointment time
     * @param excludedStatus status to exclude (typically CANCELLED)
     * @param excludeId      appointment ID to exclude (null for new appointments)
     * @return true if an active booking exists, false otherwise
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.dentist.id = :dentistId " +
            "AND a.appointmentDate = :date AND a.appointmentTime = :time " +
            "AND a.status != :excludedStatus AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean isDentistBooked(@org.springframework.data.repository.query.Param("dentistId") Long dentistId,
                            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date,
                            @org.springframework.data.repository.query.Param("time") java.time.LocalTime time,
                            @org.springframework.data.repository.query.Param("excludedStatus") com.dental.management.entity.enums.AppointmentStatus excludedStatus,
                            @org.springframework.data.repository.query.Param("excludeId") Long excludeId);

    /**
     * Search and filter appointments by optional patient, dentist, date, and status.
     * Results are ordered by appointment date and time descending.
     *
     * @param patientId optional patient filter
     * @param dentistId optional dentist filter
     * @param date      optional date filter
     * @param status    optional status filter
     * @return matching appointments
     */
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "(:dentistId IS NULL OR a.dentist.id = :dentistId) AND " +
            "(:date IS NULL OR a.appointmentDate = :date) AND " +
            "(:status IS NULL OR a.status = :status) " +
            "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findWithFilters(
            @org.springframework.data.repository.query.Param("patientId") Long patientId,
            @org.springframework.data.repository.query.Param("dentistId") Long dentistId,
            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date,
            @org.springframework.data.repository.query.Param("status") com.dental.management.entity.enums.AppointmentStatus status
    );
}

