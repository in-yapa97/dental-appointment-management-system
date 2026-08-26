package com.dental.management.service;

import com.dental.management.dto.PatientRequest;
import com.dental.management.dto.PatientResponse;
import com.dental.management.entity.Appointment;
import com.dental.management.entity.Patient;
import com.dental.management.entity.enums.Gender;
import com.dental.management.exception.DuplicatePatientNumberException;
import com.dental.management.exception.PatientDeletionException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.repository.AppointmentRepository;
import com.dental.management.repository.PatientRepository;
import com.dental.management.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient samplePatient;
    private PatientRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePatient = new Patient(
                "PAT-001",
                "John Doe",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                "+1-555-0101",
                "john.doe@example.com",
                "123 Maple Street"
        );
        samplePatient.setId(1L);

        sampleRequest = new PatientRequest(
                "PAT-001",
                "John Doe",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                "+1-555-0101",
                "john.doe@example.com",
                "123 Maple Street"
        );
    }

    @Nested
    @DisplayName("Create Patient Tests")
    class CreatePatientTests {

        @Test
        @DisplayName("Should successfully create a new patient")
        void shouldCreatePatientSuccessfully() {
            when(patientRepository.existsByPatientNumber("PAT-001")).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

            PatientResponse response = patientService.createPatient(sampleRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getPatientNumber()).isEqualTo("PAT-001");
            assertThat(response.getFullName()).isEqualTo("John Doe");
            assertThat(response.getGender()).isEqualTo(Gender.MALE);
            assertThat(response.getPhone()).isEqualTo("+1-555-0101");
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should reject patient creation on duplicate patient number")
        void shouldRejectDuplicatePatientNumber() {
            when(patientRepository.existsByPatientNumber("PAT-001")).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(sampleRequest))
                    .isInstanceOf(DuplicatePatientNumberException.class)
                    .hasMessageContaining("PAT-001");

            verify(patientRepository, never()).save(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Get Patient Tests")
    class GetPatientTests {

        @Test
        @DisplayName("Should retrieve patient by valid ID")
        void shouldReturnPatientById() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));

            PatientResponse response = patientService.getPatientById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when patient ID does not exist")
        void shouldThrowWhenPatientNotFound() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.getPatientById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("List & Search Patient Tests")
    class ListAndSearchTests {

        @Test
        @DisplayName("Should return all patients")
        void shouldReturnAllPatients() {
            when(patientRepository.findAll()).thenReturn(List.of(samplePatient));

            List<PatientResponse> responses = patientService.getAllPatients();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getPatientNumber()).isEqualTo("PAT-001");
        }

        @Test
        @DisplayName("Should return search results matching keyword")
        void shouldReturnMatchingPatients() {
            when(patientRepository.searchPatients("Doe")).thenReturn(List.of(samplePatient));

            List<PatientResponse> responses = patientService.searchPatients("Doe");

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should return all patients when search keyword is empty")
        void shouldReturnAllWhenSearchKeywordEmpty() {
            when(patientRepository.findAll()).thenReturn(List.of(samplePatient));

            List<PatientResponse> responses = patientService.searchPatients("   ");

            assertThat(responses).hasSize(1);
            verify(patientRepository, never()).searchPatients(anyString());
        }
    }

    @Nested
    @DisplayName("Update Patient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("Should successfully update patient when patient number unchanged")
        void shouldUpdatePatientSuccessfully() {
            PatientRequest updateRequest = new PatientRequest(
                    "PAT-001",
                    "John Updated",
                    LocalDate.of(1990, 5, 15),
                    Gender.MALE,
                    "+1-555-9999",
                    "john.updated@example.com",
                    "456 Oak Avenue"
            );

            when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

            PatientResponse response = patientService.updatePatient(1L, updateRequest);

            assertThat(response).isNotNull();
            assertThat(samplePatient.getFullName()).isEqualTo("John Updated");
            assertThat(samplePatient.getPhone()).isEqualTo("+1-555-9999");
            verify(patientRepository).save(samplePatient);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent patient")
        void shouldThrowWhenUpdatingNonExistentPatient() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.updatePatient(999L, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should reject update if new patient number belongs to another patient")
        void shouldRejectUpdateWithConflictingPatientNumber() {
            PatientRequest updateRequest = new PatientRequest(
                    "PAT-002",
                    "John Doe",
                    LocalDate.of(1990, 5, 15),
                    Gender.MALE,
                    "+1-555-0101",
                    "john.doe@example.com",
                    "123 Maple Street"
            );

            when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
            when(patientRepository.existsByPatientNumber("PAT-002")).thenReturn(true);

            assertThatThrownBy(() -> patientService.updatePatient(1L, updateRequest))
                    .isInstanceOf(DuplicatePatientNumberException.class)
                    .hasMessageContaining("PAT-002");

            verify(patientRepository, never()).save(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("Should delete patient when no appointments exist")
        void shouldDeletePatientSuccessfully() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
            when(appointmentRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());

            patientService.deletePatient(1L);

            verify(patientRepository).delete(samplePatient);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent patient")
        void shouldThrowWhenDeletingNonExistent() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.deletePatient(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(patientRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw PatientDeletionException when patient has existing appointments")
        void shouldPreventDeletionWithAppointments() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
            when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(new Appointment()));

            assertThatThrownBy(() -> patientService.deletePatient(1L))
                    .isInstanceOf(PatientDeletionException.class)
                    .hasMessageContaining("appointments exist");

            verify(patientRepository, never()).delete(any());
        }
    }
}
