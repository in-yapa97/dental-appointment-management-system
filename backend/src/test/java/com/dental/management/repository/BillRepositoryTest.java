package com.dental.management.repository;

import com.dental.management.entity.*;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.entity.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BillRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BillRepository billRepository;

    private Appointment persistSampleAppointment(String apptNum) {
        Patient patient = entityManager.persist(new Patient("PAT-BILL-" + apptNum, "Bill Patient",
                LocalDate.of(1988, 12, 1), Gender.OTHER, "+1-555-999-0000", null, null));
        Dentist dentist = entityManager.persist(new Dentist("DEN-BILL-" + apptNum, "Dr. Biller",
                "General", "+1-555-888-0000", null));
        Treatment treatment = entityManager.persist(new Treatment("TRT-BILL-" + apptNum, "Checkup",
                null, new BigDecimal("60.00")));

        return entityManager.persist(new Appointment(apptNum, patient, dentist, treatment,
                LocalDate.of(2026, 9, 25), LocalTime.of(11, 0), AppointmentStatus.COMPLETED, null));
    }

    @Test
    @DisplayName("Save and find bill by billNumber and appointmentId")
    void shouldSaveAndFindByBillNumberAndAppointmentId() {
        Appointment appointment = persistSampleAppointment("APP-BILL-01");

        Bill bill = new Bill(
                "BILL-2026-001",
                appointment,
                new BigDecimal("30.00"),
                new BigDecimal("60.00"),
                new BigDecimal("90.00"),
                LocalDate.of(2026, 9, 25),
                BillStatus.PAID
        );

        Bill saved = billRepository.save(bill);
        assertNotNull(saved.getId());

        Optional<Bill> found = billRepository.findByBillNumber("BILL-2026-001");
        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("90.00").compareTo(found.get().getTotalAmount()));
        assertEquals(BillStatus.PAID, found.get().getStatus());
        assertEquals("APP-BILL-01", found.get().getAppointment().getAppointmentNumber());

        Optional<Bill> byAppt = billRepository.findByAppointmentId(appointment.getId());
        assertTrue(byAppt.isPresent());
        assertEquals("BILL-2026-001", byAppt.get().getBillNumber());
    }

    @Test
    @DisplayName("Duplicate billNumber throws DataIntegrityViolationException")
    void duplicateBillNumberShouldFail() {
        Appointment a1 = persistSampleAppointment("APP-B1");
        Appointment a2 = persistSampleAppointment("APP-B2");

        Bill b1 = new Bill("BILL-DUP", a1, BigDecimal.ZERO, new BigDecimal("50.00"),
                new BigDecimal("50.00"), LocalDate.now(), BillStatus.PENDING);
        billRepository.saveAndFlush(b1);

        Bill b2 = new Bill("BILL-DUP", a2, BigDecimal.ZERO, new BigDecimal("50.00"),
                new BigDecimal("50.00"), LocalDate.now(), BillStatus.PENDING);

        assertThrows(DataIntegrityViolationException.class, () -> {
            billRepository.saveAndFlush(b2);
        });
    }

    @Test
    @DisplayName("Multiple bills for same appointment throws DataIntegrityViolationException")
    void multipleBillsForSameAppointmentShouldFail() {
        Appointment appointment = persistSampleAppointment("APP-ONE-BILL");

        Bill b1 = new Bill("BILL-A", appointment, BigDecimal.ZERO, new BigDecimal("50.00"),
                new BigDecimal("50.00"), LocalDate.now(), BillStatus.PENDING);
        billRepository.saveAndFlush(b1);

        Bill b2 = new Bill("BILL-B", appointment, BigDecimal.ZERO, new BigDecimal("50.00"),
                new BigDecimal("50.00"), LocalDate.now(), BillStatus.PENDING);

        assertThrows(DataIntegrityViolationException.class, () -> {
            billRepository.saveAndFlush(b2);
        });
    }

    @Test
    @DisplayName("Reporting queries aggregate revenue and group by treatment correctly")
    void testReportingAggregations() {
        Appointment a1 = persistSampleAppointment("APP-R1");
        Appointment a2 = persistSampleAppointment("APP-R2");

        Bill b1 = new Bill("BILL-R1", a1, new BigDecimal("20.00"), new BigDecimal("60.00"),
                new BigDecimal("80.00"), LocalDate.of(2026, 8, 1), BillStatus.PAID);
        billRepository.saveAndFlush(b1);

        Bill b2 = new Bill("BILL-R2", a2, new BigDecimal("10.00"), new BigDecimal("60.00"),
                new BigDecimal("70.00"), LocalDate.of(2026, 8, 2), BillStatus.PENDING);
        billRepository.saveAndFlush(b2);

        BigDecimal paidTotal = billRepository.sumTotalAmountByStatus(BillStatus.PAID);
        assertEquals(0, new BigDecimal("80.00").compareTo(paidTotal));

        long paidCount = billRepository.countByStatus(BillStatus.PAID);
        assertEquals(1, paidCount);

        long pendingCount = billRepository.countByStatus(BillStatus.PENDING);
        assertEquals(1, pendingCount);

        var treatmentRevenue = billRepository.findTreatmentRevenue();
        assertFalse(treatmentRevenue.isEmpty());
        assertEquals("Checkup", treatmentRevenue.get(0).getTreatmentName());
    }
}
