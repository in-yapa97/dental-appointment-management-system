package com.dental.management.controller;

import com.dental.management.config.SecurityConfig;
import com.dental.management.dto.BillRequest;
import com.dental.management.dto.BillResponse;
import com.dental.management.dto.ReceiptResponse;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.exception.BillDeletionException;
import com.dental.management.exception.DuplicateBillException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.security.JwtAuthenticationEntryPoint;
import com.dental.management.security.JwtAuthenticationFilter;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BillController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class, JwtUtils.class})
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillService billService;

    private BillRequest validRequest;
    private BillResponse sampleResponse;
    private ReceiptResponse sampleReceipt;

    @BeforeEach
    void setUp() {
        validRequest = new BillRequest(
                10L,
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                LocalDate.of(2026, 11, 20),
                BillStatus.PENDING
        );

        sampleResponse = new BillResponse(
                100L,
                "BIL-2026-X1",
                10L,
                "APT-2026-A1",
                1L,
                "Sarah Connor",
                "PAT-1001",
                2L,
                "Dr. Gregory House",
                3L,
                "Scaling and Polishing",
                "TRT-3001",
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("250.00"),
                LocalDate.of(2026, 11, 20),
                BillStatus.PENDING,
                Instant.now()
        );

        sampleReceipt = new ReceiptResponse();
        sampleReceipt.setReceiptNumber("REC-2026-X1");
        sampleReceipt.setBillNumber("BIL-2026-X1");
        sampleReceipt.setAppointmentNumber("APT-2026-A1");
        sampleReceipt.setPatientName("Sarah Connor");
        sampleReceipt.setDentistName("Dr. Gregory House");
        sampleReceipt.setTreatmentName("Scaling and Polishing");
        sampleReceipt.setTotalAmount(new BigDecimal("250.00"));
        sampleReceipt.setPaymentStatus(BillStatus.PAID);
    }

    @Nested
    @DisplayName("Authenticated Billing Endpoints")
    @WithMockUser(username = "clinic_staff", roles = {"STAFF"})
    class AuthenticatedTests {

        @Test
        @DisplayName("POST /api/v1/bills - 201 Created on valid request")
        void shouldCreateBill() throws Exception {
            when(billService.createBill(any(BillRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/bills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(100L))
                    .andExpect(jsonPath("$.billNumber").value("BIL-2026-X1"))
                    .andExpect(jsonPath("$.totalAmount").value(250.00));
        }

        @Test
        @DisplayName("POST /api/v1/bills - 400 Bad Request when appointmentId is missing")
        void shouldRejectMissingAppointmentId() throws Exception {
            BillRequest invalid = new BillRequest(null, new BigDecimal("50.00"), null, null, null);

            mockMvc.perform(post("/api/v1/bills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.appointmentId").exists());
        }

        @Test
        @DisplayName("POST /api/v1/bills - 409 Conflict when duplicate bill exists")
        void shouldReturnConflictOnDuplicateBill() throws Exception {
            when(billService.createBill(any(BillRequest.class)))
                    .thenThrow(new DuplicateBillException("A billing record already exists for appointment #APT-2026-A1"));

            mockMvc.perform(post("/api/v1/bills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Duplicate Bill"));
        }

        @Test
        @DisplayName("GET /api/v1/bills - 200 OK with list")
        void shouldGetBills() throws Exception {
            when(billService.getBills(any(), any(), any(), any(), any()))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/bills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(100L))
                    .andExpect(jsonPath("$[0].billNumber").value("BIL-2026-X1"));
        }

        @Test
        @DisplayName("GET /api/v1/bills/{id} - 200 OK when found")
        void shouldGetBillById() throws Exception {
            when(billService.getBillById(100L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/bills/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100L));
        }

        @Test
        @DisplayName("GET /api/v1/bills/{id} - 404 Not Found when missing")
        void shouldReturnNotFoundById() throws Exception {
            when(billService.getBillById(999L))
                    .thenThrow(new ResourceNotFoundException("Bill not found with id: 999"));

            mockMvc.perform(get("/api/v1/bills/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("GET /api/v1/bills/appointment/{appointmentId} - 200 OK")
        void shouldGetBillByAppointment() throws Exception {
            when(billService.getBillByAppointmentId(10L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/bills/appointment/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appointmentId").value(10L));
        }

        @Test
        @DisplayName("PUT /api/v1/bills/{id} - 200 OK on update")
        void shouldUpdateBill() throws Exception {
            when(billService.updateBill(eq(100L), any(BillRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/v1/bills/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100L));
        }

        @Test
        @DisplayName("DELETE /api/v1/bills/{id} - 200 OK on safe delete")
        void shouldDeleteBill() throws Exception {
            doNothing().when(billService).deleteBill(100L);

            mockMvc.perform(delete("/api/v1/bills/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Bill deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /api/v1/bills/{id} - 409 Conflict when bill is PAID")
        void shouldRejectDeletingPaidBill() throws Exception {
            doThrow(new BillDeletionException("Cannot delete bill because an associated payment has been completed."))
                    .when(billService).deleteBill(100L);

            mockMvc.perform(delete("/api/v1/bills/100"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Cannot Delete Bill"));
        }

        @Test
        @DisplayName("GET /api/v1/bills/{id}/receipt - 200 OK with receipt")
        void shouldGetReceipt() throws Exception {
            when(billService.getReceipt(100L)).thenReturn(sampleReceipt);

            mockMvc.perform(get("/api/v1/bills/100/receipt"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.receiptNumber").value("REC-2026-X1"))
                    .andExpect(jsonPath("$.clinicName").exists());
        }
    }

    @Nested
    @DisplayName("Unauthenticated Security Tests")
    class UnauthenticatedTests {

        @Test
        @DisplayName("GET /api/v1/bills without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedGet() throws Exception {
            mockMvc.perform(get("/api/v1/bills"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/bills without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedPost() throws Exception {
            mockMvc.perform(post("/api/v1/bills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/bills/{id}/receipt without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedReceipt() throws Exception {
            mockMvc.perform(get("/api/v1/bills/100/receipt"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
