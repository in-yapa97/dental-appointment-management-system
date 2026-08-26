package com.dental.management.service;

import com.dental.management.dto.BillRequest;
import com.dental.management.dto.BillResponse;
import com.dental.management.dto.ReceiptResponse;
import com.dental.management.entity.enums.BillStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface defining business operations for Billing, Invoicing,
 * and Receipt generation.
 */
public interface BillService {

    /**
     * Create and issue an invoice/bill for an appointment.
     *
     * @param request billing details
     * @return created bill representation
     */
    BillResponse createBill(BillRequest request);

    /**
     * Retrieve a bill by its unique identifier.
     *
     * @param id bill ID
     * @return bill representation
     */
    BillResponse getBillById(Long id);

    /**
     * Retrieve a bill associated with a specific appointment.
     *
     * @param appointmentId appointment ID
     * @return bill representation
     */
    BillResponse getBillByAppointmentId(Long appointmentId);

    /**
     * Retrieve bills matching optional filters.
     *
     * @param patientId     optional patient ID filter
     * @param appointmentId optional appointment ID filter
     * @param status        optional payment status filter
     * @param date          optional bill date filter
     * @param billNumber    optional bill number substring filter
     * @return list of matching bills
     */
    List<BillResponse> getBills(Long patientId, Long appointmentId, BillStatus status, LocalDate date, String billNumber);

    /**
     * Update bill financial fees, date, and payment status.
     *
     * @param id      bill ID
     * @param request updated billing details
     * @return updated bill representation
     */
    BillResponse updateBill(Long id, BillRequest request);

    /**
     * Safely delete a bill (only allowed if not PAID).
     *
     * @param id bill ID
     */
    void deleteBill(Long id);

    /**
     * Generate an official receipt for the bill.
     *
     * @param id bill ID
     * @return official receipt representation
     */
    ReceiptResponse getReceipt(Long id);
}
