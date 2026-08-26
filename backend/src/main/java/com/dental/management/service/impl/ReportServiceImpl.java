package com.dental.management.service.impl;

import com.dental.management.dto.PaymentStatusReportResponse;
import com.dental.management.dto.RevenueReportResponse;
import com.dental.management.dto.TreatmentRevenueResponse;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.repository.BillRepository;
import com.dental.management.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation aggregating financial statistics, revenue,
 * and treatment profitability metrics.
 */
@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BillRepository billRepository;

    public ReportServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    public RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to) {
        BigDecimal totalRevenue;
        BigDecimal paidAmount;
        BigDecimal unpaidAmount;
        long totalBills;
        long paidBills;
        long pendingBills;
        long cancelledBills;
        long refundedBills;

        if (from == null && to == null) {
            totalRevenue = billRepository.sumTotalAmountByStatus(BillStatus.PAID);
            paidAmount = totalRevenue;
            unpaidAmount = billRepository.sumTotalAmountByStatus(BillStatus.PENDING);
            totalBills = billRepository.count();
            paidBills = billRepository.countByStatus(BillStatus.PAID);
            pendingBills = billRepository.countByStatus(BillStatus.PENDING);
            cancelledBills = billRepository.countByStatus(BillStatus.CANCELLED);
            refundedBills = billRepository.countByStatus(BillStatus.REFUNDED);
        } else {
            LocalDate startDate = from != null ? from : LocalDate.of(2000, 1, 1);
            LocalDate endDate = to != null ? to : LocalDate.now().plusYears(10);

            totalRevenue = billRepository.sumTotalAmountByStatusAndDateBetween(BillStatus.PAID, startDate, endDate);
            paidAmount = totalRevenue;
            unpaidAmount = billRepository.sumTotalAmountByStatusAndDateBetween(BillStatus.PENDING, startDate, endDate);
            totalBills = billRepository.countByDateBetween(startDate, endDate);
            paidBills = billRepository.countByStatusAndDateBetween(BillStatus.PAID, startDate, endDate);
            pendingBills = billRepository.countByStatusAndDateBetween(BillStatus.PENDING, startDate, endDate);
            cancelledBills = billRepository.countByStatusAndDateBetween(BillStatus.CANCELLED, startDate, endDate);
            refundedBills = billRepository.countByStatusAndDateBetween(BillStatus.REFUNDED, startDate, endDate);
        }

        return new RevenueReportResponse(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                paidAmount != null ? paidAmount : BigDecimal.ZERO,
                unpaidAmount != null ? unpaidAmount : BigDecimal.ZERO,
                totalBills,
                paidBills,
                pendingBills,
                cancelledBills,
                refundedBills,
                from,
                to
        );
    }

    @Override
    public PaymentStatusReportResponse getPaymentStatusReport() {
        List<PaymentStatusReportResponse.StatusItem> breakdown = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        long totalCount = 0;

        for (BillStatus status : BillStatus.values()) {
            long count = billRepository.countByStatus(status);
            BigDecimal sum = billRepository.sumTotalAmountByStatus(status);
            if (sum == null) sum = BigDecimal.ZERO;

            breakdown.add(new PaymentStatusReportResponse.StatusItem(status, count, sum));
            totalAmount = totalAmount.add(sum);
            totalCount += count;
        }

        return new PaymentStatusReportResponse(breakdown, totalCount, totalAmount);
    }

    @Override
    public List<TreatmentRevenueResponse> getTreatmentRevenueReport() {
        return billRepository.findTreatmentRevenue();
    }
}
