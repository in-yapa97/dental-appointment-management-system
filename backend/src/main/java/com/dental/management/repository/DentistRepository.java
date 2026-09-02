package com.dental.management.repository;

import com.dental.management.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Dentist entity operations.
 */
@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long> {

    /**
     * Find a dentist by their unique dentist identifier.
     *
     * @param dentistNumber the dentist number to look up
     * @return Optional containing the Dentist if found, or empty
     */
    Optional<Dentist> findByDentistNumber(String dentistNumber);

    /**
     * Check if a dentist exists with the given dentist number.
     *
     * @param dentistNumber the dentist number to check
     * @return true if a record exists, false otherwise
     */
    boolean existsByDentistNumber(String dentistNumber);

    /**
     * Find all dentists filtered by active status.
     *
     * @param active whether the dentist is active
     * @return List of active/inactive dentists
     */
    List<Dentist> findByActive(boolean active);

    /**
     * Multi-field search across dentist number, full name, specialization, phone, and email.
     *
     * @param keyword search keyword
     * @return list of matching dentists
     */
    @Query("SELECT d FROM Dentist d WHERE " +
            "LOWER(d.dentistNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dentist> searchDentists(@Param("keyword") String keyword);
}
