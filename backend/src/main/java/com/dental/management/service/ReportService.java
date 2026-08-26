package com.dental.management.service;

import com.dental.management.dto.PaymentStatusReportResponse;
import com.dental.management.dto.RevenueReportResponse;
import com.dental.management.dto.TreatmentRevenueResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for generating financial and operational reports.
 */
public interface ReportService {

    /**
     * Generate high-level revenue metrics, optionally filtered by date range.
     *
     * @param from optional start date
     * @param to   optional end date
     * @return revenue report metrics
     */
    RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to);

    /**
     * Generate financial metrics broken down by payment status.
     *
     * @return payment status breakdown
     */
    PaymentStatusReportResponse getPaymentStatusReport();

    /**
     * Generate revenue metrics grouped by treatment procedure.
     *
     * @return treatment revenue breakdown
     */
    List<TreatmentRevenueResponse> getTreatmentRevenueReport();
}
