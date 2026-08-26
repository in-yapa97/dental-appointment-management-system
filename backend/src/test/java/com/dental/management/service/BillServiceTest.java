package com.dental.management.service;

import com.dental.management.dto.BillRequest;
import com.dental.management.dto.BillResponse;
import com.dental.management.dto.ReceiptResponse;
import com.dental.management.entity.Appointment;
import com.dental.management.entity.Bill;
import com.dental.management.entity.Dentist;
import com.dental.management.entity.Patient;
import com.dental.management.entity.Treatment;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.entity.enums.Gender;
import com.dental.management.exception.BillDeletionException;
import com.dental.management.exception.DuplicateBillException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.repository.AppointmentRepository;
import com.dental.management.repository.BillRepository;
import com.dental.management.service.impl.BillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private BillServiceImpl billService;

    private Patient patient;
    private Dentist dentist;
    private Treatment treatment;
    private Appointment appointment;
    private Bill bill;
    private BillRequest validRequest;

    @BeforeEach
    void setUp() {
        patient = new Patient("PAT-001", "John Doe", LocalDate.of(1990, 1, 1),
                Gender.MALE, "+1-555-1111", "john@example.com", "Main Street");
        patient.setId(1L);

        dentist = new Dentist("DEN-001", "Dr. Smith", "Orthodontics",
                "+1-555-2222", "smith@clinic.com");
        dentist.setId(2L);

        treatment = new Treatment("TRT-001", "Root Canal", "Endodontic therapy", new BigDecimal("350.00"));
        treatment.setId(3L);

        appointment = new Appointment("APT-TEST01", patient, dentist, treatment,
                LocalDate.of(2026, 11, 15), LocalTime.of(10, 0),
                AppointmentStatus.SCHEDULED, "First consultation");
        appointment.setId(10L);

        bill = new Bill("BIL-TEST01", appointment, new BigDecimal("50.00"),
                new BigDecimal("350.00"), new BigDecimal("400.00"),
                LocalDate.of(2026, 11, 15), BillStatus.PENDING);
        bill.setId(100L);

        validRequest = new BillRequest(10L, new BigDecimal("50.00"), new BigDecimal("350.00"),
                LocalDate.of(2026, 11, 15), BillStatus.PENDING);
    }

    @Nested
    @DisplayName("Create Bill Tests")
    class CreateBillTests {

        @Test
        @DisplayName("Create bill successfully with fee calculations")
        void shouldCreateBillSuccessfully() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());
            when(billRepository.existsByBillNumber(anyString())).thenReturn(false);
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            BillResponse response = billService.createBill(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getBillNumber()).isEqualTo("BIL-TEST01");
            assertThat(response.getConsultationFee()).isEqualByComparingTo("50.00");
            assertThat(response.getTreatmentAmount()).isEqualByComparingTo("350.00");
            assertThat(response.getTotalAmount()).isEqualByComparingTo("400.00");
            verify(billRepository).save(any(Bill.class));
        }

        @Test
        @DisplayName("Missing appointment throws ResourceNotFoundException")
        void shouldThrowWhenAppointmentNotFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
            validRequest.setAppointmentId(99L);

            assertThatThrownBy(() -> billService.createBill(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Appointment not found");
        }

        @Test
        @DisplayName("Duplicate bill for same appointment throws DuplicateBillException")
        void shouldThrowWhenDuplicateBillExists() {
            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(10L)).thenReturn(Optional.of(bill));

            assertThatThrownBy(() -> billService.createBill(validRequest))
                    .isInstanceOf(DuplicateBillException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Default fees applied when fees not supplied")
        void shouldApplyDefaultFeesWhenNull() {
            BillRequest reqWithoutFees = new BillRequest(10L, null, null, null, null);

            when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());
            when(billRepository.existsByBillNumber(anyString())).thenReturn(false);
            when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
                Bill b = invocation.getArgument(0);
                b.setId(101L);
                return b;
            });

            BillResponse response = billService.createBill(reqWithoutFees);

            assertThat(response.getConsultationFee()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getTreatmentAmount()).isEqualByComparingTo("350.00");
            assertThat(response.getTotalAmount()).isEqualByComparingTo("350.00");
            assertThat(response.getStatus()).isEqualTo(BillStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Read & Filter Tests")
    class ReadAndFilterTests {

        @Test
        @DisplayName("Get bill by ID successfully")
        void shouldGetBillById() {
            when(billRepository.findById(100L)).thenReturn(Optional.of(bill));

            BillResponse response = billService.getBillById(100L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getPatientName()).isEqualTo("John Doe");
            assertThat(response.getDentistName()).isEqualTo("Dr. Smith");
        }

        @Test
        @DisplayName("Get missing bill throws ResourceNotFoundException")
        void shouldThrowWhenBillNotFound() {
            when(billRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> billService.getBillById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Get bill by appointment ID successfully")
        void shouldGetBillByAppointmentId() {
            when(billRepository.findByAppointmentId(10L)).thenReturn(Optional.of(bill));

            BillResponse response = billService.getBillByAppointmentId(10L);

            assertThat(response).isNotNull();
            assertThat(response.getAppointmentId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Get bills with Specification filters")
        void shouldGetBillsWithFilters() {
            when(billRepository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of(bill));

            List<BillResponse> results = billService.getBills(null, null, null, null, null);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getBillNumber()).isEqualTo("BIL-TEST01");
        }
    }

    @Nested
    @DisplayName("Update & Safe Delete Tests")
    class UpdateAndDeleteTests {

        @Test
        @DisplayName("Update bill amounts and recalculate total")
        void shouldUpdateBillAmounts() {
            when(billRepository.findById(100L)).thenReturn(Optional.of(bill));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            BillRequest updateReq = new BillRequest();
            updateReq.setConsultationFee(new BigDecimal("75.00"));
            updateReq.setStatus(BillStatus.PAID);

            BillResponse response = billService.updateBill(100L, updateReq);

            assertThat(response).isNotNull();
            verify(billRepository).save(bill);
            assertThat(bill.getConsultationFee()).isEqualByComparingTo("75.00");
            assertThat(bill.getTotalAmount()).isEqualByComparingTo("425.00");
            assertThat(bill.getStatus()).isEqualTo(BillStatus.PAID);
        }

        @Test
        @DisplayName("Delete PENDING bill successfully")
        void shouldDeletePendingBill() {
            when(billRepository.findById(100L)).thenReturn(Optional.of(bill));

            billService.deleteBill(100L);

            verify(billRepository).delete(any(Bill.class));
        }

        @Test
        @DisplayName("Delete PAID bill rejected with BillDeletionException")
        void shouldRejectDeletingPaidBill() {
            bill.setStatus(BillStatus.PAID);
            when(billRepository.findById(100L)).thenReturn(Optional.of(bill));

            assertThatThrownBy(() -> billService.deleteBill(100L))
                    .isInstanceOf(BillDeletionException.class)
                    .hasMessageContaining("Paid");

            verify(billRepository, never()).delete(any(Bill.class));
        }
    }

    @Nested
    @DisplayName("Receipt Tests")
    class ReceiptTests {

        @Test
        @DisplayName("Generate receipt with complete clinic and procedure information")
        void shouldGenerateReceipt() {
            when(billRepository.findById(100L)).thenReturn(Optional.of(bill));

            ReceiptResponse receipt = billService.getReceipt(100L);

            assertThat(receipt).isNotNull();
            assertThat(receipt.getReceiptNumber()).contains("REC-");
            assertThat(receipt.getBillNumber()).isEqualTo("BIL-TEST01");
            assertThat(receipt.getPatientName()).isEqualTo("John Doe");
            assertThat(receipt.getDentistName()).isEqualTo("Dr. Smith");
            assertThat(receipt.getTreatmentName()).isEqualTo("Root Canal");
            assertThat(receipt.getTotalAmount()).isEqualByComparingTo("400.00");
            assertThat(receipt.getClinicName()).isNotBlank();
        }
    }
}
