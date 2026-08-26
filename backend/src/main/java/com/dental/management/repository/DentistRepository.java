package com.dental.management.repository;

import com.dental.management.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
