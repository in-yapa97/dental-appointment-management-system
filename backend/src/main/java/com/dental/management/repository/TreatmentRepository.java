package com.dental.management.repository;

import com.dental.management.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Treatment entity operations.
 */
@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    /**
     * Find a treatment by its unique treatment code.
     *
     * @param treatmentCode the treatment code to search
     * @return Optional containing the Treatment if found, or empty
     */
    Optional<Treatment> findByTreatmentCode(String treatmentCode);

    /**
     * Check if a treatment exists with the given treatment code.
     *
     * @param treatmentCode the treatment code to verify
     * @return true if a record exists, false otherwise
     */
    boolean existsByTreatmentCode(String treatmentCode);

    /**
     * Find all treatments filtered by active status.
     *
     * @param active whether the treatment is active
     * @return List of active/inactive treatments
     */
    List<Treatment> findByActive(boolean active);
}
