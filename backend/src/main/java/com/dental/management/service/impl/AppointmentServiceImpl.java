package com.dental.management.service.impl;

import com.dental.management.dto.*;
import com.dental.management.entity.Appointment;
import com.dental.management.entity.Dentist;
import com.dental.management.entity.Patient;
import com.dental.management.entity.Treatment;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.exception.*;
import com.dental.management.repository.*;
import com.dental.management.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing appointment bookings, dentist schedule
 * conflict prevention, multi-parameter filtering, and billing relational safety.
 */
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final BillRepository billRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  DentistRepository dentistRepository,
                                  TreatmentRepository treatmentRepository,
                                  BillRepository billRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.treatmentRepository = treatmentRepository;
        this.billRepository = billRepository;
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // 1. Verify Patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        // 2. Verify Dentist exists and is active
        Dentist dentist = dentistRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + request.getDentistId()));
        if (!dentist.isActive()) {
            throw new InactiveResourceException("Dentist '" + dentist.getFullName() + "' is currently inactive and cannot be scheduled.");
        }

        // 3. Verify Treatment exists and is active
        Treatment treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found with id: " + request.getTreatmentId()));
        if (!treatment.isActive()) {
            throw new InactiveResourceException("Treatment '" + treatment.getTreatmentName() + "' is currently inactive and cannot be scheduled.");
        }

        // 4. Dentist availability & conflict check
        boolean booked = appointmentRepository.isDentistBooked(
                dentist.getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.CANCELLED,
                null
        );
        if (booked) {
            throw new DentistUnavailableException("Dentist '" + dentist.getFullName() + "' is not available on " +
                    request.getAppointmentDate() + " at " + request.getAppointmentTime());
        }

        // 5. Generate server-controlled unique appointment number
        String appointmentNumber = generateUniqueAppointmentNumber();

        Appointment appointment = new Appointment(
                appointmentNumber,
                patient,
                dentist,
                treatment,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                request.getStatus() != null ? request.getStatus() : AppointmentStatus.SCHEDULED,
                request.getNotes()
        );

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return AppointmentResponse.fromEntity(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments(Long patientId, Long dentistId, LocalDate date, AppointmentStatus status) {
        org.springframework.data.jpa.domain.Specification<Appointment> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (patientId != null) {
                predicates.add(cb.equal(root.get("patient").get("id"), patientId));
            }
            if (dentistId != null) {
                predicates.add(cb.equal(root.get("dentist").get("id"), dentistId));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("appointmentDate"), date));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "appointmentDate", "appointmentTime");

        return appointmentRepository.findAll(spec, sort)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        // Verify entities
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Dentist dentist = dentistRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + request.getDentistId()));
        if (!dentist.isActive()) {
            throw new InactiveResourceException("Dentist '" + dentist.getFullName() + "' is currently inactive.");
        }

        Treatment treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found with id: " + request.getTreatmentId()));
        if (!treatment.isActive()) {
            throw new InactiveResourceException("Treatment '" + treatment.getTreatmentName() + "' is currently inactive.");
        }

        // Re-check conflict if schedule or dentist altered
        boolean scheduleChanged = !existing.getDentist().getId().equals(request.getDentistId())
                || !existing.getAppointmentDate().equals(request.getAppointmentDate())
                || !existing.getAppointmentTime().equals(request.getAppointmentTime());

        if (scheduleChanged) {
            boolean booked = appointmentRepository.isDentistBooked(
                    dentist.getId(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime(),
                    AppointmentStatus.CANCELLED,
                    existing.getId()
            );
            if (booked) {
                throw new DentistUnavailableException("Dentist '" + dentist.getFullName() + "' is not available on " +
                        request.getAppointmentDate() + " at " + request.getAppointmentTime());
            }
        }

        existing.setPatient(patient);
        existing.setDentist(dentist);
        existing.setTreatment(treatment);
        existing.setAppointmentDate(request.getAppointmentDate());
        existing.setAppointmentTime(request.getAppointmentTime());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(existing);
        return AppointmentResponse.fromEntity(saved);
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        // Relational safety: prevent deleting appointments with associated billing records
        if (billRepository.findByAppointmentId(id).isPresent()) {
            throw new AppointmentDeletionException("Cannot delete appointment #" + existing.getAppointmentNumber() +
                    " because an associated billing record exists in the system.");
        }

        appointmentRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long dentistId, LocalDate date, LocalTime time) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + dentistId));

        if (!dentist.isActive()) {
            return AvailabilityResponse.unavailable("Dentist '" + dentist.getFullName() + "' is currently inactive");
        }

        boolean booked = appointmentRepository.isDentistBooked(
                dentistId,
                date,
                time,
                AppointmentStatus.CANCELLED,
                null
        );

        if (booked) {
            return AvailabilityResponse.unavailable("Dentist '" + dentist.getFullName() + "' is already booked for " + date + " at " + time);
        }

        return AvailabilityResponse.available();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> getActiveDentists() {
        return dentistRepository.findByActive(true)
                .stream()
                .map(DentistResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponse> getActiveTreatments() {
        return treatmentRepository.findByActive(true)
                .stream()
                .map(TreatmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private String generateUniqueAppointmentNumber() {
        for (int i = 0; i < 5; i++) {
            String candidate = "APT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            if (!appointmentRepository.existsByAppointmentNumber(candidate)) {
                return candidate;
            }
        }
        throw new DuplicateAppointmentNumberException("Unable to generate unique appointment identifier. Please retry.");
    }
}
