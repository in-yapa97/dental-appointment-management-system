package com.dental.management.controller;

import com.dental.management.config.SecurityConfig;
import com.dental.management.dto.PaymentStatusReportResponse;
import com.dental.management.dto.RevenueReportResponse;
import com.dental.management.dto.TreatmentRevenueResponse;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.security.JwtAuthenticationEntryPoint;
import com.dental.management.security.JwtAuthenticationFilter;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class, JwtUtils.class})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Nested
    @DisplayName("Authenticated Report Endpoints")
    @WithMockUser(username = "clinic_admin", roles = {"STAFF"})
    class AuthenticatedTests {

        @Test
        @DisplayName("GET /api/v1/reports/revenue - 200 OK")
        void shouldGetRevenueReport() throws Exception {
            RevenueReportResponse response = new RevenueReportResponse(
                    new BigDecimal("5000.00"),
                    new BigDecimal("5000.00"),
                    new BigDecimal("1200.00"),
                    20L, 15L, 3L, 2L, 0L, null, null
            );

            when(reportService.getRevenueReport(any(), any())).thenReturn(response);

            mockMvc.perform(get("/api/v1/reports/revenue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRevenue").value(5000.00))
                    .andExpect(jsonPath("$.unpaidAmount").value(1200.00))
                    .andExpect(jsonPath("$.totalBills").value(20));
        }

        @Test
        @DisplayName("GET /api/v1/reports/payment-status - 200 OK")
        void shouldGetPaymentStatusReport() throws Exception {
            PaymentStatusReportResponse response = new PaymentStatusReportResponse(
                    List.of(new PaymentStatusReportResponse.StatusItem(BillStatus.PAID, 15L, new BigDecimal("5000.00"))),
                    15L,
                    new BigDecimal("5000.00")
            );

            when(reportService.getPaymentStatusReport()).thenReturn(response);

            mockMvc.perform(get("/api/v1/reports/payment-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(15))
                    .andExpect(jsonPath("$.breakdown[0].status").value("PAID"));
        }

        @Test
        @DisplayName("GET /api/v1/reports/treatment-revenue - 200 OK")
        void shouldGetTreatmentRevenueReport() throws Exception {
            TreatmentRevenueResponse item = new TreatmentRevenueResponse(
                    1L, "TRT-001", "Scaling", 10L, new BigDecimal("1500.00")
            );

            when(reportService.getTreatmentRevenueReport()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/v1/reports/treatment-revenue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].treatmentCode").value("TRT-001"))
                    .andExpect(jsonPath("$[0].totalRevenue").value(1500.00));
        }
    }

    @Nested
    @DisplayName("Unauthenticated Security Tests")
    class UnauthenticatedTests {

        @Test
        @DisplayName("GET /api/v1/reports/revenue without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedRevenue() throws Exception {
            mockMvc.perform(get("/api/v1/reports/revenue"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/reports/payment-status without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedPaymentStatus() throws Exception {
            mockMvc.perform(get("/api/v1/reports/payment-status"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/reports/treatment-revenue without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedTreatmentRevenue() throws Exception {
            mockMvc.perform(get("/api/v1/reports/treatment-revenue"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
