package com.dental.management.service;

import com.dental.management.dto.PaymentStatusReportResponse;
import com.dental.management.dto.RevenueReportResponse;
import com.dental.management.dto.TreatmentRevenueResponse;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.repository.BillRepository;
import com.dental.management.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("Generate all-time revenue report")
    void shouldGenerateAllTimeRevenueReport() {
        when(billRepository.sumTotalAmountByStatus(BillStatus.PAID)).thenReturn(new BigDecimal("1500.00"));
        when(billRepository.sumTotalAmountByStatus(BillStatus.PENDING)).thenReturn(new BigDecimal("400.00"));
        when(billRepository.count()).thenReturn(10L);
        when(billRepository.countByStatus(BillStatus.PAID)).thenReturn(7L);
        when(billRepository.countByStatus(BillStatus.PENDING)).thenReturn(2L);
        when(billRepository.countByStatus(BillStatus.CANCELLED)).thenReturn(1L);
        when(billRepository.countByStatus(BillStatus.REFUNDED)).thenReturn(0L);

        RevenueReportResponse report = reportService.getRevenueReport(null, null);

        assertThat(report).isNotNull();
        assertThat(report.getTotalRevenue()).isEqualByComparingTo("1500.00");
        assertThat(report.getPaidAmount()).isEqualByComparingTo("1500.00");
        assertThat(report.getUnpaidAmount()).isEqualByComparingTo("400.00");
        assertThat(report.getTotalBills()).isEqualTo(10L);
        assertThat(report.getPaidBills()).isEqualTo(7L);
    }

    @Test
    @DisplayName("Generate revenue report with date range filters")
    void shouldGenerateDateFilteredRevenueReport() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(billRepository.sumTotalAmountByStatusAndDateBetween(eq(BillStatus.PAID), eq(from), eq(to)))
                .thenReturn(new BigDecimal("850.00"));
        when(billRepository.sumTotalAmountByStatusAndDateBetween(eq(BillStatus.PENDING), eq(from), eq(to)))
                .thenReturn(new BigDecimal("150.00"));
        when(billRepository.countByDateBetween(eq(from), eq(to))).thenReturn(5L);
        when(billRepository.countByStatusAndDateBetween(eq(BillStatus.PAID), eq(from), eq(to))).thenReturn(4L);
        when(billRepository.countByStatusAndDateBetween(eq(BillStatus.PENDING), eq(from), eq(to))).thenReturn(1L);
        when(billRepository.countByStatusAndDateBetween(eq(BillStatus.CANCELLED), eq(from), eq(to))).thenReturn(0L);
        when(billRepository.countByStatusAndDateBetween(eq(BillStatus.REFUNDED), eq(from), eq(to))).thenReturn(0L);

        RevenueReportResponse report = reportService.getRevenueReport(from, to);

        assertThat(report.getTotalRevenue()).isEqualByComparingTo("850.00");
        assertThat(report.getTotalBills()).isEqualTo(5L);
        assertThat(report.getFromDate()).isEqualTo(from);
        assertThat(report.getToDate()).isEqualTo(to);
    }

    @Test
    @DisplayName("Generate payment status breakdown report")
    void shouldGeneratePaymentStatusReport() {
        when(billRepository.countByStatus(BillStatus.PAID)).thenReturn(5L);
        when(billRepository.sumTotalAmountByStatus(BillStatus.PAID)).thenReturn(new BigDecimal("1000.00"));

        when(billRepository.countByStatus(BillStatus.PENDING)).thenReturn(2L);
        when(billRepository.sumTotalAmountByStatus(BillStatus.PENDING)).thenReturn(new BigDecimal("300.00"));

        when(billRepository.countByStatus(BillStatus.CANCELLED)).thenReturn(1L);
        when(billRepository.sumTotalAmountByStatus(BillStatus.CANCELLED)).thenReturn(new BigDecimal("150.00"));

        when(billRepository.countByStatus(BillStatus.REFUNDED)).thenReturn(0L);
        when(billRepository.sumTotalAmountByStatus(BillStatus.REFUNDED)).thenReturn(BigDecimal.ZERO);

        PaymentStatusReportResponse report = reportService.getPaymentStatusReport();

        assertThat(report.getTotalCount()).isEqualTo(8L);
        assertThat(report.getTotalAmount()).isEqualByComparingTo("1450.00");
        assertThat(report.getBreakdown()).hasSize(4);
    }

    @Test
    @DisplayName("Generate treatment revenue report")
    void shouldGenerateTreatmentRevenueReport() {
        TreatmentRevenueResponse t1 = new TreatmentRevenueResponse(1L, "TRT-001", "Extraction", 3L, new BigDecimal("450.00"));
        TreatmentRevenueResponse t2 = new TreatmentRevenueResponse(2L, "TRT-002", "Cleaning", 5L, new BigDecimal("600.00"));

        when(billRepository.findTreatmentRevenue()).thenReturn(List.of(t2, t1));

        List<TreatmentRevenueResponse> list = reportService.getTreatmentRevenueReport();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getTreatmentName()).isEqualTo("Cleaning");
        assertThat(list.get(0).getTotalRevenue()).isEqualByComparingTo("600.00");
    }
}
