package com.dental.management.service.impl;

import com.dental.management.dto.BillRequest;
import com.dental.management.dto.BillResponse;
import com.dental.management.dto.ReceiptResponse;
import com.dental.management.entity.Appointment;
import com.dental.management.entity.Bill;
import com.dental.management.entity.enums.BillStatus;
import com.dental.management.exception.BillDeletionException;
import com.dental.management.exception.DuplicateBillException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.repository.AppointmentRepository;
import com.dental.management.repository.BillRepository;
import com.dental.management.service.BillService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing invoice lifecycle, fee calculation,
 * payment transitions, and financial auditing safety.
 */
@Service
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final AppointmentRepository appointmentRepository;

    public BillServiceImpl(BillRepository billRepository, AppointmentRepository appointmentRepository) {
        this.billRepository = billRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public BillResponse createBill(BillRequest request) {
        // 1. Verify Appointment exists
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));

        // 2. Prevent duplicate billing records for the same appointment (OneToOne)
        if (billRepository.findByAppointmentId(request.getAppointmentId()).isPresent()) {
            throw new DuplicateBillException("A billing record already exists for appointment #" + appointment.getAppointmentNumber());
        }

        // 3. Calculate amounts
        BigDecimal consultationFee = request.getConsultationFee() != null ? request.getConsultationFee() : BigDecimal.ZERO;
        BigDecimal treatmentAmount = request.getTreatmentAmount() != null ? request.getTreatmentAmount() :
                (appointment.getTreatment() != null ? appointment.getTreatment().getCost() : BigDecimal.ZERO);
        BigDecimal totalAmount = consultationFee.add(treatmentAmount);

        // 4. Generate unique bill number
        String billNumber = generateUniqueBillNumber();

        LocalDate billDate = request.getBillDate() != null ? request.getBillDate() : LocalDate.now();
        BillStatus status = request.getStatus() != null ? request.getStatus() : BillStatus.PENDING;

        Bill bill = new Bill(billNumber, appointment, consultationFee, treatmentAmount, totalAmount, billDate, status);
        Bill saved = billRepository.save(bill);

        return BillResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        return BillResponse.fromEntity(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByAppointmentId(Long appointmentId) {
        Bill bill = billRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No billing record found for appointment id: " + appointmentId));
        return BillResponse.fromEntity(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getBills(Long patientId, Long appointmentId, BillStatus status, LocalDate date, String billNumber) {
        Specification<Bill> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (patientId != null) {
                predicates.add(cb.equal(root.get("appointment").get("patient").get("id"), patientId));
            }
            if (appointmentId != null) {
                predicates.add(cb.equal(root.get("appointment").get("id"), appointmentId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("billDate"), date));
            }
            if (billNumber != null && !billNumber.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("billNumber")), "%" + billNumber.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "billDate", "id");
        return billRepository.findAll(spec, sort)
                .stream()
                .map(BillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BillResponse updateBill(Long id, BillRequest request) {
        Bill existing = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        if (request.getConsultationFee() != null) {
            existing.setConsultationFee(request.getConsultationFee());
        }
        if (request.getTreatmentAmount() != null) {
            existing.setTreatmentAmount(request.getTreatmentAmount());
        }

        // Recalculate total
        existing.setTotalAmount(existing.getConsultationFee().add(existing.getTreatmentAmount()));

        if (request.getBillDate() != null) {
            existing.setBillDate(request.getBillDate());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        Bill saved = billRepository.save(existing);
        return BillResponse.fromEntity(saved);
    }

    @Override
    public void deleteBill(Long id) {
        Bill existing = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        // Financial Safety: Paid bills cannot be removed
        if (existing.getStatus() == BillStatus.PAID) {
            throw new BillDeletionException("Cannot delete bill #" + existing.getBillNumber() +
                    " because an associated payment has been completed. Paid bills must be preserved for financial auditing.");
        }

        billRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceipt(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        return ReceiptResponse.fromBill(bill);
    }

    private String generateUniqueBillNumber() {
        for (int i = 0; i < 5; i++) {
            String candidate = "BIL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            if (!billRepository.existsByBillNumber(candidate)) {
                return candidate;
            }
        }
        throw new DuplicateBillException("Unable to generate unique bill number. Please retry.");
    }
}
