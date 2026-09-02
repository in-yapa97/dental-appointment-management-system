package com.dental.management.service;

import com.dental.management.dto.DentistRequest;
import com.dental.management.dto.DentistResponse;
import com.dental.management.entity.Appointment;
import com.dental.management.entity.Dentist;
import com.dental.management.exception.DentistDeletionException;
import com.dental.management.exception.DuplicateDentistNumberException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.repository.AppointmentRepository;
import com.dental.management.repository.DentistRepository;
import com.dental.management.service.impl.DentistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DentistServiceTest {

    @Mock
    private DentistRepository dentistRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DentistServiceImpl dentistService;

    private Dentist sampleDentist;
    private DentistRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleDentist = new Dentist(
                "DEN-001",
                "Dr. Marcus Vance",
                "Orthodontics",
                "+1-555-0101",
                "marcus.vance@dentalcare.com"
        );
        sampleDentist.setId(1L);

        sampleRequest = new DentistRequest(
                "DEN-001",
                "Dr. Marcus Vance",
                "Orthodontics",
                "+1-555-0101",
                "marcus.vance@dentalcare.com",
                true
        );
    }

    @Nested
    @DisplayName("Create Dentist Tests")
    class CreateDentistTests {

        @Test
        @DisplayName("Should successfully create a dentist when number is unique")
        void shouldCreateDentistSuccessfully() {
            when(dentistRepository.existsByDentistNumber("DEN-001")).thenReturn(false);
            when(dentistRepository.save(any(Dentist.class))).thenAnswer(invocation -> {
                Dentist d = invocation.getArgument(0);
                d.setId(1L);
                return d;
            });

            DentistResponse response = dentistService.createDentist(sampleRequest);

            assertThat(response).isNotNull();
            assertThat(response.getDentistNumber()).isEqualTo("DEN-001");
            assertThat(response.getFullName()).isEqualTo("Dr. Marcus Vance");
            assertThat(response.getSpecialization()).isEqualTo("Orthodontics");
            assertThat(response.isActive()).isTrue();
            verify(dentistRepository).save(any(Dentist.class));
        }

        @Test
        @DisplayName("Should throw DuplicateDentistNumberException when dentist number exists")
        void shouldThrowWhenDentistNumberExists() {
            when(dentistRepository.existsByDentistNumber("DEN-001")).thenReturn(true);

            assertThatThrownBy(() -> dentistService.createDentist(sampleRequest))
                    .isInstanceOf(DuplicateDentistNumberException.class)
                    .hasMessageContaining("DEN-001");

            verify(dentistRepository, never()).save(any(Dentist.class));
        }
    }

    @Nested
    @DisplayName("Read and Search Tests")
    class ReadAndSearchTests {

        @Test
        @DisplayName("Should return dentist by ID when found")
        void shouldReturnDentistById() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(sampleDentist));

            DentistResponse response = dentistService.getDentistById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFullName()).isEqualTo("Dr. Marcus Vance");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
        void shouldThrowWhenDentistNotFound() {
            when(dentistRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dentistService.getDentistById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Should return all dentists")
        void shouldReturnAllDentists() {
            when(dentistRepository.findAll()).thenReturn(List.of(sampleDentist));

            List<DentistResponse> list = dentistService.getAllDentists();

            assertThat(list).hasSize(1);
            assertThat(list.get(0).getDentistNumber()).isEqualTo("DEN-001");
        }

        @Test
        @DisplayName("Should search dentists with keyword")
        void shouldSearchDentistsWithKeyword() {
            when(dentistRepository.searchDentists("Marcus")).thenReturn(List.of(sampleDentist));

            List<DentistResponse> list = dentistService.searchDentists("Marcus");

            assertThat(list).hasSize(1);
            assertThat(list.get(0).getFullName()).isEqualTo("Dr. Marcus Vance");
        }

        @Test
        @DisplayName("Should return all dentists when keyword is empty")
        void shouldReturnAllWhenKeywordEmpty() {
            when(dentistRepository.findAll()).thenReturn(List.of(sampleDentist));

            List<DentistResponse> list = dentistService.searchDentists("   ");

            assertThat(list).hasSize(1);
            verify(dentistRepository, never()).searchDentists(anyString());
        }
    }

    @Nested
    @DisplayName("Update and Delete Tests")
    class UpdateAndDeleteTests {

        @Test
        @DisplayName("Should successfully update dentist")
        void shouldUpdateDentistSuccessfully() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(sampleDentist));
            when(dentistRepository.save(any(Dentist.class))).thenReturn(sampleDentist);

            DentistRequest updateRequest = new DentistRequest(
                    "DEN-001",
                    "Dr. Marcus Vance Updated",
                    "Pediatric Dentistry",
                    "+1-555-0999",
                    "marcus.updated@dentalcare.com",
                    true
            );

            DentistResponse response = dentistService.updateDentist(1L, updateRequest);

            assertThat(response).isNotNull();
            verify(dentistRepository).save(sampleDentist);
        }

        @Test
        @DisplayName("Should throw DuplicateDentistNumberException when updated number belongs to another dentist")
        void shouldThrowWhenUpdatedNumberExists() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(sampleDentist));
            when(dentistRepository.existsByDentistNumber("DEN-002")).thenReturn(true);

            DentistRequest conflictRequest = new DentistRequest(
                    "DEN-002",
                    "Dr. Marcus Vance",
                    "Orthodontics",
                    "+1-555-0101",
                    "marcus.vance@dentalcare.com",
                    true
            );

            assertThatThrownBy(() -> dentistService.updateDentist(1L, conflictRequest))
                    .isInstanceOf(DuplicateDentistNumberException.class);
        }

        @Test
        @DisplayName("Should successfully delete dentist when no appointments exist")
        void shouldDeleteDentistSuccessfully() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(sampleDentist));
            when(appointmentRepository.findByDentistId(1L)).thenReturn(Collections.emptyList());

            dentistService.deleteDentist(1L);

            verify(dentistRepository).delete(sampleDentist);
        }

        @Test
        @DisplayName("Should throw DentistDeletionException when appointments are attached")
        void shouldThrowWhenAppointmentsAttached() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(sampleDentist));
            when(appointmentRepository.findByDentistId(1L)).thenReturn(List.of(mock(Appointment.class)));

            assertThatThrownBy(() -> dentistService.deleteDentist(1L))
                    .isInstanceOf(DentistDeletionException.class)
                    .hasMessageContaining("Cannot delete dentist");

            verify(dentistRepository, never()).delete(any(Dentist.class));
        }
    }
}
