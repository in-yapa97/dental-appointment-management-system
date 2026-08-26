package com.dental.management.dto;

import com.dental.management.entity.enums.BillStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO representing financial metrics broken down by payment status.
 */
public class PaymentStatusReportResponse {

    private List<StatusItem> breakdown;
    private long totalCount;
    private BigDecimal totalAmount;

    public PaymentStatusReportResponse() {
    }

    public PaymentStatusReportResponse(List<StatusItem> breakdown, long totalCount, BigDecimal totalAmount) {
        this.breakdown = breakdown;
        this.totalCount = totalCount;
        this.totalAmount = totalAmount;
    }

    public List<StatusItem> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(List<StatusItem> breakdown) {
        this.breakdown = breakdown;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static class StatusItem {
        private BillStatus status;
        private long count;
        private BigDecimal totalAmount;

        public StatusItem() {
        }

        public StatusItem(BillStatus status, long count, BigDecimal totalAmount) {
            this.status = status;
            this.count = count;
            this.totalAmount = totalAmount;
        }

        public BillStatus getStatus() {
            return status;
        }

        public void setStatus(BillStatus status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
