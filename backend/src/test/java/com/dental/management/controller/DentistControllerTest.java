package com.dental.management.controller;

import com.dental.management.config.SecurityConfig;
import com.dental.management.dto.DentistRequest;
import com.dental.management.dto.DentistResponse;
import com.dental.management.exception.DentistDeletionException;
import com.dental.management.exception.DuplicateDentistNumberException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.security.JwtAuthenticationEntryPoint;
import com.dental.management.security.JwtAuthenticationFilter;
import com.dental.management.security.JwtUtils;
import com.dental.management.service.DentistService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DentistController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class, JwtUtils.class})
class DentistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DentistService dentistService;

    private DentistRequest validRequest;
    private DentistResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = new DentistRequest(
                "DEN-100",
                "Dr. Sarah Connor",
                "Oral Surgery",
                "+1-555-0202",
                "sarah.connor@dentalcare.com",
                true
        );

        sampleResponse = new DentistResponse(
                1L,
                "DEN-100",
                "Dr. Sarah Connor",
                "Oral Surgery",
                "+1-555-0202",
                "sarah.connor@dentalcare.com",
                true
        );
    }

    @Nested
    @DisplayName("Unauthenticated Access")
    class UnauthenticatedTests {

        @Test
        @DisplayName("POST /api/v1/dentists without auth returns 401 Unauthorized")
        void createDentistWithoutAuthReturns401() throws Exception {
            mockMvc.perform(post("/api/v1/dentists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/dentists without auth returns 401 Unauthorized")
        void getAllDentistsWithoutAuthReturns401() throws Exception {
            mockMvc.perform(get("/api/v1/dentists"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authenticated Staff Access")
    @WithMockUser(roles = "STAFF")
    class AuthenticatedTests {

        @Test
        @DisplayName("POST /api/v1/dentists with valid data returns 201 Created")
        void createDentistWithValidDataReturns201() throws Exception {
            when(dentistService.createDentist(any(DentistRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/dentists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.dentistNumber").value("DEN-100"))
                    .andExpect(jsonPath("$.fullName").value("Dr. Sarah Connor"))
                    .andExpect(jsonPath("$.specialization").value("Oral Surgery"))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("POST /api/v1/dentists with missing required fields returns 400 Bad Request")
        void createDentistWithMissingFieldsReturns400() throws Exception {
            DentistRequest invalid = new DentistRequest("", "", "", "", "not-an-email", true);

            mockMvc.perform(post("/api/v1/dentists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.dentistNumber").exists())
                    .andExpect(jsonPath("$.errors.fullName").exists());
        }

        @Test
        @DisplayName("POST /api/v1/dentists with duplicate dentistNumber returns 409 Conflict")
        void createDentistDuplicateNumberReturns409() throws Exception {
            when(dentistService.createDentist(any(DentistRequest.class)))
                    .thenThrow(new DuplicateDentistNumberException("Dentist number 'DEN-100' is already registered"));

            mockMvc.perform(post("/api/v1/dentists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Dentist Number Already Exists"));
        }

        @Test
        @DisplayName("GET /api/v1/dentists returns 200 OK with list")
        void getAllDentistsReturns200() throws Exception {
            when(dentistService.getAllDentists()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/dentists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].dentistNumber").value("DEN-100"));
        }

        @Test
        @DisplayName("GET /api/v1/dentists/search?keyword=Sarah returns 200 OK with results")
        void searchDentistsReturns200() throws Exception {
            when(dentistService.searchDentists("Sarah")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/dentists/search").param("keyword", "Sarah"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].fullName").value("Dr. Sarah Connor"));
        }

        @Test
        @DisplayName("GET /api/v1/dentists/{id} returns 200 OK when found")
        void getDentistByIdReturns200() throws Exception {
            when(dentistService.getDentistById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/dentists/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Dr. Sarah Connor"));
        }

        @Test
        @DisplayName("GET /api/v1/dentists/{id} returns 404 Not Found when not found")
        void getDentistByIdNotFoundReturns404() throws Exception {
            when(dentistService.getDentistById(99L))
                    .thenThrow(new ResourceNotFoundException("Dentist not found with id: 99"));

            mockMvc.perform(get("/api/v1/dentists/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("PUT /api/v1/dentists/{id} with valid data returns 200 OK")
        void updateDentistReturns200() throws Exception {
            when(dentistService.updateDentist(eq(1L), any(DentistRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/v1/dentists/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("DELETE /api/v1/dentists/{id} when no appointments returns 200 OK")
        void deleteDentistReturns200() throws Exception {
            doNothing().when(dentistService).deleteDentist(1L);

            mockMvc.perform(delete("/api/v1/dentists/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Dentist deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /api/v1/dentists/{id} with appointments returns 409 Conflict")
        void deleteDentistWithAppointmentsReturns409() throws Exception {
            doThrow(new DentistDeletionException("Cannot delete dentist 'Dr. Sarah Connor' because appointments exist."))
                    .when(dentistService).deleteDentist(1L);

            mockMvc.perform(delete("/api/v1/dentists/1"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Cannot Delete Dentist"));
        }
    }
}
