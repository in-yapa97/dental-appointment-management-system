package com.dental.management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO representing high-level clinic revenue metrics.
 */
public class RevenueReportResponse {

    private BigDecimal totalRevenue;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
    private long totalBills;
    private long paidBills;
    private long pendingBills;
    private long cancelledBills;
    private long refundedBills;
    private LocalDate fromDate;
    private LocalDate toDate;

    public RevenueReportResponse() {
    }

    public RevenueReportResponse(BigDecimal totalRevenue, BigDecimal paidAmount, BigDecimal unpaidAmount,
                                 long totalBills, long paidBills, long pendingBills, long cancelledBills, long refundedBills,
                                 LocalDate fromDate, LocalDate toDate) {
        this.totalRevenue = totalRevenue;
        this.paidAmount = paidAmount;
        this.unpaidAmount = unpaidAmount;
        this.totalBills = totalBills;
        this.paidBills = paidBills;
        this.pendingBills = pendingBills;
        this.cancelledBills = cancelledBills;
        this.refundedBills = refundedBills;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(BigDecimal unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public long getTotalBills() {
        return totalBills;
    }

    public void setTotalBills(long totalBills) {
        this.totalBills = totalBills;
    }

    public long getPaidBills() {
        return paidBills;
    }

    public void setPaidBills(long paidBills) {
        this.paidBills = paidBills;
    }

    public long getPendingBills() {
        return pendingBills;
    }

    public void setPendingBills(long pendingBills) {
        this.pendingBills = pendingBills;
    }

    public long getCancelledBills() {
        return cancelledBills;
    }

    public void setCancelledBills(long cancelledBills) {
        this.cancelledBills = cancelledBills;
    }

    public long getRefundedBills() {
        return refundedBills;
    }

    public void setRefundedBills(long refundedBills) {
        this.refundedBills = refundedBills;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
