package com.dental.management.controller;

import com.dental.management.config.SecurityConfig;
import com.dental.management.dto.AppointmentRequest;
import com.dental.management.dto.AppointmentResponse;
import com.dental.management.dto.AvailabilityResponse;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.exception.AppointmentDeletionException;
import com.dental.management.exception.DentistUnavailableException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.security.JwtAuthenticationEntryPoint;
import com.dental.management.security.JwtAuthenticationFilter;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.AppointmentService;
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
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class, JwtUtils.class})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private AppointmentRequest validRequest;
    private AppointmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = new AppointmentRequest(
                1L,
                2L,
                3L,
                LocalDate.of(2026, 9, 20),
                LocalTime.of(14, 30),
                AppointmentStatus.SCHEDULED,
                "Regular consultation"
        );

        sampleResponse = new AppointmentResponse(
                100L,
                "APT-2026-X1",
                1L,
                "Alice Wonder",
                "PAT-001",
                2L,
                "Dr. Smith",
                "DEN-001",
                3L,
                "Teeth Whitening",
                "TRT-001",
                new BigDecimal("200.00"),
                LocalDate.of(2026, 9, 20),
                LocalTime.of(14, 30),
                AppointmentStatus.SCHEDULED,
                "Regular consultation",
                Instant.now()
        );
    }

    @Nested
    @DisplayName("Authenticated Appointment Endpoints")
    @WithMockUser(username = "clinic_staff", roles = {"STAFF"})
    class AuthenticatedTests {

        @Test
        @DisplayName("POST /api/v1/appointments - 201 Created on valid booking")
        void shouldCreateAppointment() throws Exception {
            when(appointmentService.createAppointment(any(AppointmentRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(100L))
                    .andExpect(jsonPath("$.appointmentNumber").value("APT-2026-X1"))
                    .andExpect(jsonPath("$.patientName").value("Alice Wonder"))
                    .andExpect(jsonPath("$.dentistName").value("Dr. Smith"));
        }

        @Test
        @DisplayName("POST /api/v1/appointments - 400 Bad Request on invalid fields")
        void shouldRejectInvalidAppointment() throws Exception {
            AppointmentRequest invalid = new AppointmentRequest(null, null, null, null, null, null, null);

            mockMvc.perform(post("/api/v1/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.patientId").exists())
                    .andExpect(jsonPath("$.errors.dentistId").exists())
                    .andExpect(jsonPath("$.errors.treatmentId").exists())
                    .andExpect(jsonPath("$.errors.appointmentDate").exists())
                    .andExpect(jsonPath("$.errors.appointmentTime").exists());
        }

        @Test
        @DisplayName("POST /api/v1/appointments - 409 Conflict when dentist unavailable")
        void shouldReturnConflictWhenDentistBooked() throws Exception {
            when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                    .thenThrow(new DentistUnavailableException("Dentist Dr. Smith is not available on 2026-09-20 at 14:30"));

            mockMvc.perform(post("/api/v1/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Dentist Dr. Smith is not available on 2026-09-20 at 14:30"));
        }

        @Test
        @DisplayName("GET /api/v1/appointments - 200 OK with list")
        void shouldGetAppointments() throws Exception {
            when(appointmentService.getAppointments(any(), any(), any(), any()))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/appointments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(100L))
                    .andExpect(jsonPath("$[0].appointmentNumber").value("APT-2026-X1"));
        }

        @Test
        @DisplayName("GET /api/v1/appointments/{id} - 200 OK when found")
        void shouldGetAppointmentById() throws Exception {
            when(appointmentService.getAppointmentById(100L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/appointments/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100L));
        }

        @Test
        @DisplayName("GET /api/v1/appointments/{id} - 404 Not Found when missing")
        void shouldReturnNotFoundById() throws Exception {
            when(appointmentService.getAppointmentById(999L))
                    .thenThrow(new ResourceNotFoundException("Appointment not found with id: 999"));

            mockMvc.perform(get("/api/v1/appointments/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("PUT /api/v1/appointments/{id} - 200 OK on valid update")
        void shouldUpdateAppointment() throws Exception {
            when(appointmentService.updateAppointment(eq(100L), any(AppointmentRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put("/api/v1/appointments/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100L));
        }

        @Test
        @DisplayName("DELETE /api/v1/appointments/{id} - 200 OK on safe deletion")
        void shouldDeleteAppointment() throws Exception {
            doNothing().when(appointmentService).deleteAppointment(100L);

            mockMvc.perform(delete("/api/v1/appointments/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Appointment deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /api/v1/appointments/{id} - 409 Conflict when bill exists")
        void shouldRejectDeletionWithBill() throws Exception {
            doThrow(new AppointmentDeletionException("Cannot delete appointment because a billing record exists"))
                    .when(appointmentService).deleteAppointment(100L);

            mockMvc.perform(delete("/api/v1/appointments/100"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("GET /api/v1/appointments/availability - 200 OK with availability status")
        void shouldCheckAvailability() throws Exception {
            when(appointmentService.checkAvailability(eq(2L), any(), any()))
                    .thenReturn(AvailabilityResponse.available());

            mockMvc.perform(get("/api/v1/appointments/availability")
                            .param("dentistId", "2")
                            .param("date", "2026-09-20")
                            .param("time", "14:30:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(true));
        }
    }

    @Nested
    @DisplayName("Unauthenticated Security Tests")
    class UnauthenticatedTests {

        @Test
        @DisplayName("GET /api/v1/appointments without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedGet() throws Exception {
            mockMvc.perform(get("/api/v1/appointments"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/appointments without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedPost() throws Exception {
            mockMvc.perform(post("/api/v1/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/appointments/availability without JWT returns 401 Unauthorized")
        void shouldRejectUnauthenticatedAvailability() throws Exception {
            mockMvc.perform(get("/api/v1/appointments/availability")
                            .param("dentistId", "2")
                            .param("date", "2026-09-20")
                            .param("time", "14:30:00"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
