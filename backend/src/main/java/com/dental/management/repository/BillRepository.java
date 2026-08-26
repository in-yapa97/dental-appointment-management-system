package com.dental.management.repository;

import com.dental.management.dto.TreatmentRevenueResponse;
import com.dental.management.entity.Bill;
import com.dental.management.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Bill entity operations.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

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

    /**
     * Count bills by status.
     */
    long countByStatus(BillStatus status);

    /**
     * Sum totalAmount by status.
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") BillStatus status);

    /**
     * Sum totalAmount by status and date range.
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.status = :status AND b.billDate >= :from AND b.billDate <= :to")
    BigDecimal sumTotalAmountByStatusAndDateBetween(@Param("status") BillStatus status,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    /**
     * Count bills by status and date range.
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status AND b.billDate >= :from AND b.billDate <= :to")
    long countByStatusAndDateBetween(@Param("status") BillStatus status,
                                    @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);

    /**
     * Count total bills within a date range.
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.billDate >= :from AND b.billDate <= :to")
    long countByDateBetween(@Param("from") LocalDate from,
                            @Param("to") LocalDate to);

    /**
     * Group revenue by dental treatment procedure.
     */
    @Query("SELECT new com.dental.management.dto.TreatmentRevenueResponse(" +
           "t.id, t.treatmentCode, t.treatmentName, COUNT(b), COALESCE(SUM(b.treatmentAmount), 0)) " +
           "FROM Bill b JOIN b.appointment a JOIN a.treatment t " +
           "WHERE b.status = com.dental.management.entity.enums.BillStatus.PAID " +
           "GROUP BY t.id, t.treatmentCode, t.treatmentName " +
           "ORDER BY SUM(b.treatmentAmount) DESC")
    List<TreatmentRevenueResponse> findTreatmentRevenue();
}

