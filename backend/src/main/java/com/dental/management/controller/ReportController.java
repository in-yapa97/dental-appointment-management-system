package com.dental.management.controller;

import com.dental.management.dto.PaymentStatusReportResponse;
import com.dental.management.dto.RevenueReportResponse;
import com.dental.management.dto.TreatmentRevenueResponse;
import com.dental.management.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller delivering financial reports, revenue summaries,
 * and operational metrics.
 * Requires JWT authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Retrieve total revenue metrics, optionally filtered by date range.
     *
     * @param from optional start date
     * @param to   optional end date
     * @return 200 OK with revenue report
     */
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        RevenueReportResponse report = reportService.getRevenueReport(from, to);
        return ResponseEntity.ok(report);
    }

    /**
     * Retrieve financial metrics grouped by bill payment status.
     *
     * @return 200 OK with payment status breakdown
     */
    @GetMapping("/payment-status")
    public ResponseEntity<PaymentStatusReportResponse> getPaymentStatusReport() {
        PaymentStatusReportResponse report = reportService.getPaymentStatusReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Retrieve revenue metrics grouped by dental treatment procedure.
     *
     * @return 200 OK with treatment revenue breakdown
     */
    @GetMapping("/treatment-revenue")
    public ResponseEntity<List<TreatmentRevenueResponse>> getTreatmentRevenueReport() {
        List<TreatmentRevenueResponse> report = reportService.getTreatmentRevenueReport();
        return ResponseEntity.ok(report);
    }
}
