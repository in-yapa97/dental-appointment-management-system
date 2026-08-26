package com.dental.management.controller;

import com.dental.management.config.SecurityConfig;
import com.dental.management.dto.PatientRequest;
import com.dental.management.dto.PatientResponse;
import com.dental.management.entity.enums.Gender;
import com.dental.management.exception.DuplicatePatientNumberException;
import com.dental.management.exception.PatientDeletionException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.security.JwtAuthenticationEntryPoint;
import com.dental.management.security.JwtAuthenticationFilter;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.PatientService;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PatientController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class, JwtUtils.class})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private PatientRequest validRequest;
    private PatientResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = new PatientRequest(
                "PAT-100",
                "Alice Smith",
                LocalDate.of(1985, 3, 20),
                Gender.FEMALE,
                "+1-555-0202",
                "alice@example.com",
                "789 Pine Road"
        );

        sampleResponse = new PatientResponse(
                10L,
                "PAT-100",
                "Alice Smith",
                LocalDate.of(1985, 3, 20),
                Gender.FEMALE,
                "+1-555-0202",
                "alice@example.com",
                "789 Pine Road",
                Instant.now()
        );
    }

    @Nested
    @DisplayName("Authenticated Patient Tests")
    @WithMockUser(username = "staffuser", roles = {"STAFF"})
    class AuthenticatedTests {

        @Test
        @DisplayName("POST /api/v1/patients - 201 Created on valid request")
        void shouldCreatePatient() throws Exception {
            when(patientService.createPatient(any(PatientRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.patientNumber").value("PAT-100"))
                    .andExpect(jsonPath("$.fullName").value("Alice Smith"))
                    .andExpect(jsonPath("$.gender").value("FEMALE"));
        }

        @Test
        @DisplayName("POST /api/v1/patients - 400 Bad Request on validation errors")
        void shouldRejectInvalidPatientCreation() throws Exception {
            PatientRequest invalid = new PatientRequest(
                    "",
                    "",
                    LocalDate.now().plusDays(1), // future date of birth invalid
                    Gender.FEMALE,
                    "invalid-phone",
                    "not-an-email",
                    ""
            );

            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.patientNumber").exists())
                    .andExpect(jsonPath("$.errors.fullName").exists())
                    .andExpect(jsonPath("$.errors.dateOfBirth").exists())
                    .andExpect(jsonPath("$.errors.phone").exists())
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @DisplayName("POST /api/v1/patients - 409 Conflict on duplicate patient number")
        void shouldReturnConflictOnDuplicateNumber() throws Exception {
            when(patientService.createPatient(any(PatientRequest.class)))
                    .thenThrow(new DuplicatePatientNumberException("Patient number 'PAT-100' is already registered"));

            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Patient number 'PAT-100' is already registered"));
        }

        @Test
        @DisplayName("GET /api/v1/patients - 200 OK with list of patients")
        void shouldReturnAllPatients() throws Exception {
            when(patientService.getAllPatients()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(10L))
                    .andExpect(jsonPath("$[0].patientNumber").value("PAT-100"));
        }

        @Test
        @DisplayName("GET /api/v1/patients/{id} - 200 OK when found")
        void shouldReturnPatientById() throws Exception {
            when(patientService.getPatientById(10L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/patients/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.fullName").value("Alice Smith"));
        }

        @Test
        @DisplayName("GET /api/v1/patients/{id} - 404 Not Found when missing")
        void shouldReturnNotFoundById() throws Exception {
            when(patientService.getPatientById(99L))
                    .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

            mockMvc.perform(get("/api/v1/patients/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("GET /api/v1/patients/search - 200 OK with matching patients")
        void shouldSearchPatients() throws Exception {
            when(patientService.searchPatients("Alice")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/patients/search").param("keyword", "Alice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].fullName").value("Alice Smith"));
        }

        @Test
        @DisplayName("PUT /api/v1/patients/{id} - 200 OK on successful update")
        void shouldUpdatePatient() throws Exception {
            when(patientService.updatePatient(eq(10L), any(PatientRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/v1/patients/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L));
        }

        @Test
        @DisplayName("PUT /api/v1/patients/{id} - 404 Not Found when updating non-existent patient")
        void shouldReturnNotFoundOnUpdate() throws Exception {
            when(patientService.updatePatient(eq(99L), any(PatientRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

            mockMvc.perform(put("/api/v1/patients/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("DELETE /api/v1/patients/{id} - 200 OK on safe delete")
        void shouldDeletePatientSuccessfully() throws Exception {
            doNothing().when(patientService).deletePatient(10L);

            mockMvc.perform(delete("/api/v1/patients/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Patient deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /api/v1/patients/{id} - 409 Conflict when appointments exist")
        void shouldRejectDeletionWithAppointments() throws Exception {
            doThrow(new PatientDeletionException("Cannot delete patient because appointments exist"))
                    .when(patientService).deletePatient(10L);

            mockMvc.perform(delete("/api/v1/patients/10"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Cannot delete patient because appointments exist"));
        }
    }

    @Nested
    @DisplayName("Unauthenticated Security Tests")
    class UnauthenticatedTests {

        @Test
        @DisplayName("GET /api/v1/patients without authentication returns 401")
        void shouldRejectUnauthenticatedGet() throws Exception {
            mockMvc.perform(get("/api/v1/patients"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/patients without authentication returns 401")
        void shouldRejectUnauthenticatedPost() throws Exception {
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
