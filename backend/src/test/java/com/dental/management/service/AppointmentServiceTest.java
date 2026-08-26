package com.dental.management.service;

import com.dental.management.dto.*;
import com.dental.management.entity.*;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.entity.enums.Gender;
import com.dental.management.exception.*;
import com.dental.management.repository.*;
import com.dental.management.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DentistRepository dentistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Dentist activeDentist;
    private Dentist inactiveDentist;
    private Treatment activeTreatment;
    private Treatment inactiveTreatment;
    private Appointment appointment;
    private AppointmentRequest validRequest;

    @BeforeEach
    void setUp() {
        patient = new Patient("PAT-001", "Jane Doe", LocalDate.of(1995, 6, 20),
                Gender.FEMALE, "+1-555-0101", "jane@example.com", "123 Street");
        patient.setId(1L);

        activeDentist = new Dentist("DEN-001", "Dr. Marcus", "Orthodontics",
                "+1-555-0202", "marcus@clinic.com");
        activeDentist.setId(2L);
        activeDentist.setActive(true);

        inactiveDentist = new Dentist("DEN-002", "Dr. Retired", "Periodontics",
                "+1-555-0303", "retired@clinic.com");
        inactiveDentist.setId(3L);
        inactiveDentist.setActive(false);

        activeTreatment = new Treatment("TRT-001", "Routine Cleaning", "Standard prophylaxis", new BigDecimal("120.00"));
        activeTreatment.setId(4L);
        activeTreatment.setActive(true);

        inactiveTreatment = new Treatment("TRT-002", "Discontinued Procedure", "Old procedure", new BigDecimal("80.00"));
        inactiveTreatment.setId(5L);
        inactiveTreatment.setActive(false);

        appointment = new Appointment("APT-TEST01", patient, activeDentist, activeTreatment,
                LocalDate.of(2026, 10, 15), LocalTime.of(10, 0),
                AppointmentStatus.SCHEDULED, "Routine check");
        appointment.setId(10L);

        validRequest = new AppointmentRequest(1L, 2L, 4L,
                LocalDate.of(2026, 10, 15), LocalTime.of(10, 0),
                AppointmentStatus.SCHEDULED, "Routine check");
    }

    @Nested
    @DisplayName("Create Appointment Tests")
    class CreateAppointmentTests {

        @Test
        @DisplayName("1. Create appointment successfully")
        void shouldCreateAppointmentSuccessfully() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(treatmentRepository.findById(4L)).thenReturn(Optional.of(activeTreatment));
            when(appointmentRepository.isDentistBooked(eq(2L), any(), any(), eq(AppointmentStatus.CANCELLED), eq(null)))
                    .thenReturn(false);
            when(appointmentRepository.existsByAppointmentNumber(anyString())).thenReturn(false);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

            AppointmentResponse response = appointmentService.createAppointment(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getPatientName()).isEqualTo("Jane Doe");
            assertThat(response.getDentistName()).isEqualTo("Dr. Marcus");
            assertThat(response.getTreatmentName()).isEqualTo("Routine Cleaning");
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("2. Patient not found throws ResourceNotFoundException")
        void shouldThrowWhenPatientNotFound() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());
            validRequest.setPatientId(99L);

            assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Patient not found");
        }

        @Test
        @DisplayName("3. Dentist not found throws ResourceNotFoundException")
        void shouldThrowWhenDentistNotFound() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(99L)).thenReturn(Optional.empty());
            validRequest.setDentistId(99L);

            assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Dentist not found");
        }

        @Test
        @DisplayName("4. Treatment not found throws ResourceNotFoundException")
        void shouldThrowWhenTreatmentNotFound() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(treatmentRepository.findById(99L)).thenReturn(Optional.empty());
            validRequest.setTreatmentId(99L);

            assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Treatment not found");
        }

        @Test
        @DisplayName("5. Inactive dentist rejected")
        void shouldRejectInactiveDentist() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(3L)).thenReturn(Optional.of(inactiveDentist));
            validRequest.setDentistId(3L);

            assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                    .isInstanceOf(InactiveResourceException.class)
                    .hasMessageContaining("inactive");
        }

        @Test
        @DisplayName("6. Inactive treatment rejected")
        void shouldRejectInactiveTreatment() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(treatmentRepository.findById(5L)).thenReturn(Optional.of(inactiveTreatment));
            validRequest.setTreatmentId(5L);

            assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                    .isInstanceOf(InactiveResourceException.class)
                    .hasMessageContaining("inactive");
        }

        @Test
        @DisplayName("7. Dentist already booked throws DentistUnavailableException")
        void shouldRejectDoubleBooking() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(treatmentRepository.findById(4L)).thenReturn(Optional.of(activeTreatment));
            when(appointmentRepository.isDentistBooked(eq(2L), any(), any(), eq(AppointmentStatus.CANCELLED), eq(null)))
                    .thenReturn(true);

            assertThatThrownBy(() -> appointmentService.createAppointment(validRequest))
                    .isInstanceOf(DentistUnavailableException.class)
                    .hasMessageContaining("not available");
        }
    }

    @Nested
    @DisplayName("Availability Tests")
    class AvailabilityTests {

        @Test
        @DisplayName("8. Availability check returns available")
        void shouldReturnAvailable() {
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(appointmentRepository.isDentistBooked(eq(2L), any(), any(), eq(AppointmentStatus.CANCELLED), eq(null)))
                    .thenReturn(false);

            AvailabilityResponse res = appointmentService.checkAvailability(2L, LocalDate.of(2026, 10, 15), LocalTime.of(10, 0));

            assertThat(res.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("9. Availability check returns unavailable")
        void shouldReturnUnavailableWhenBooked() {
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(appointmentRepository.isDentistBooked(eq(2L), any(), any(), eq(AppointmentStatus.CANCELLED), eq(null)))
                    .thenReturn(true);

            AvailabilityResponse res = appointmentService.checkAvailability(2L, LocalDate.of(2026, 10, 15), LocalTime.of(10, 0));

            assertThat(res.isAvailable()).isFalse();
            assertThat(res.getReason()).contains("already booked");
        }
    }

    @Nested
    @DisplayName("Read & Filter Tests")
    class ReadAndFilterTests {

        @Test
        @DisplayName("10. Get appointment by ID successfully")
        void shouldGetAppointmentById() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

            AppointmentResponse res = appointmentService.getAppointmentById(10L);

            assertThat(res).isNotNull();
            assertThat(res.getId()).isEqualTo(10L);
            assertThat(res.getAppointmentNumber()).isEqualTo("APT-TEST01");
        }

        @Test
        @DisplayName("11. Get missing appointment throws ResourceNotFoundException")
        void shouldThrowWhenAppointmentNotFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("12. Get all appointments / filtered")
        void shouldGetAppointmentsWithFilters() {
            when(appointmentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                    .thenReturn(List.of(appointment));

            List<AppointmentResponse> list = appointmentService.getAppointments(null, null, null, null);

            assertThat(list).hasSize(1);
            assertThat(list.get(0).getPatientName()).isEqualTo("Jane Doe");
        }
    }

    @Nested
    @DisplayName("Update & Delete Tests")
    class UpdateAndDeleteTests {

        @Test
        @DisplayName("13. Update appointment successfully")
        void shouldUpdateAppointmentSuccessfully() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(treatmentRepository.findById(4L)).thenReturn(Optional.of(activeTreatment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

            validRequest.setNotes("Updated notes");
            AppointmentResponse res = appointmentService.updateAppointment(10L, validRequest);

            assertThat(res).isNotNull();
            verify(appointmentRepository).save(appointment);
        }

        @Test
        @DisplayName("14. Update appointment with conflicting dentist/time rejected")
        void shouldRejectUpdateWithConflict() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(activeDentist));
            when(treatmentRepository.findById(4L)).thenReturn(Optional.of(activeTreatment));

            // Change time to 14:00 where dentist is booked
            validRequest.setAppointmentTime(LocalTime.of(14, 0));
            when(appointmentRepository.isDentistBooked(eq(2L), any(), eq(LocalTime.of(14, 0)), eq(AppointmentStatus.CANCELLED), eq(10L)))
                    .thenReturn(true);

            assertThatThrownBy(() -> appointmentService.updateAppointment(10L, validRequest))
                    .isInstanceOf(DentistUnavailableException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("15. Delete appointment successfully")
        void shouldDeleteAppointmentSuccessfully() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());

            appointmentService.deleteAppointment(10L);

            verify(appointmentRepository).delete(appointment);
        }

        @Test
        @DisplayName("16. Delete appointment with existing bill rejected")
        void shouldRejectDeletionWithExistingBill() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(10L)).thenReturn(Optional.of(new Bill()));

            assertThatThrownBy(() -> appointmentService.deleteAppointment(10L))
                    .isInstanceOf(AppointmentDeletionException.class)
                    .hasMessageContaining("billing record exists");

            verify(appointmentRepository, never()).delete(any(Appointment.class));
        }
    }
}
