package com.dental.management.repository;

import com.dental.management.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Bill entity operations.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Find a bill by its unique bill number.
     *
     * @param billNumber the bill identifier
     * @return Optional containing the Bill if found, or empty
     */
    Optional<Bill> findByBillNumber(String billNumber);

    /**
     * Check if a bill exists with the given bill number.
     *
     * @param billNumber the bill identifier to verify
     * @return true if a record exists, false otherwise
     */
    boolean existsByBillNumber(String billNumber);

    /**
     * Find the bill associated with a specific appointment.
     *
     * @param appointmentId the ID of the appointment
     * @return Optional containing the Bill if found, or empty
     */
    Optional<Bill> findByAppointmentId(Long appointmentId);
}
