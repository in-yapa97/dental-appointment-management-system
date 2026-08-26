package com.dental.management.repository;

import com.dental.management.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Patient entity operations.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Find a patient by their unique patient identifier.
     *
     * @param patientNumber the patient number to query
     * @return Optional containing the Patient if found, or empty
     */
    Optional<Patient> findByPatientNumber(String patientNumber);

    /**
     * Check if a patient exists with the given patient number.
     *
     * @param patientNumber the patient number to check
     * @return true if a record exists, false otherwise
     */
    boolean existsByPatientNumber(String patientNumber);

    /**
     * Case-insensitive keyword search matching patientNumber, fullName, phone, or email.
     *
     * @param keyword search keyword
     * @return list of matching patients
     */
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.patientNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    java.util.List<Patient> searchPatients(@org.springframework.data.repository.query.Param("keyword") String keyword);
}

