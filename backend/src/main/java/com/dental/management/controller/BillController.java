package com.dental.management.controller;

import com.dental.management.dto.BillRequest;
import com.dental.management.dto.BillResponse;
import com.dental.management.dto.MessageResponse;
import com.dental.management.dto.ReceiptResponse;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.service.BillService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller providing Bill CRUD, filtering, status updates,
 * safe deletion, and official receipt generation.
 * Requires JWT authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/v1/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    /**
     * Create an invoice/bill for an appointment.
     *
     * @param request billing details
     * @return 201 CREATED with bill representation
     */
    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
        BillResponse created = billService.createBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieve bills matching optional filters.
     *
     * @param patientId     optional patient ID filter
     * @param appointmentId optional appointment ID filter
     * @param status        optional status filter
     * @param date          optional date filter
     * @param billNumber    optional bill number substring filter
     * @return 200 OK with list of matching bills
     */
    @GetMapping
    public ResponseEntity<List<BillResponse>> getBills(
            @RequestParam(name = "patientId", required = false) Long patientId,
            @RequestParam(name = "appointmentId", required = false) Long appointmentId,
            @RequestParam(name = "status", required = false) BillStatus status,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "billNumber", required = false) String billNumber) {
        List<BillResponse> bills = billService.getBills(patientId, appointmentId, status, date, billNumber);
        return ResponseEntity.ok(bills);
    }

    /**
     * Retrieve bill by ID.
     *
     * @param id bill ID
     * @return 200 OK with bill representation
     */
    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Long id) {
        BillResponse bill = billService.getBillById(id);
        return ResponseEntity.ok(bill);
    }

    /**
     * Retrieve bill associated with an appointment.
     *
     * @param appointmentId appointment ID
     * @return 200 OK with bill representation
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<BillResponse> getBillByAppointmentId(@PathVariable Long appointmentId) {
        BillResponse bill = billService.getBillByAppointmentId(appointmentId);
        return ResponseEntity.ok(bill);
    }

    /**
     * Update bill fees, date, or payment status.
     *
     * @param id      bill ID
     * @param request updated billing details
     * @return 200 OK with updated bill representation
     */
    @PutMapping("/{id}")
    public ResponseEntity<BillResponse> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody BillRequest request) {
        BillResponse updated = billService.updateBill(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Safely delete a bill (only allowed if not PAID).
     *
     * @param id bill ID
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok(new MessageResponse("Bill deleted successfully"));
    }

    /**
     * Retrieve official receipt for a bill.
     *
     * @param id bill ID
     * @return 200 OK with receipt representation
     */
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable Long id) {
        ReceiptResponse receipt = billService.getReceipt(id);
        return ResponseEntity.ok(receipt);
    }
}
