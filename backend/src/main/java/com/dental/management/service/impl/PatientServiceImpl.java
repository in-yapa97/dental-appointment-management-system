package com.dental.management.service.impl;

import com.dental.management.dto.PatientRequest;
import com.dental.management.dto.PatientResponse;
import com.dental.management.entity.Patient;
import com.dental.management.exception.DuplicatePatientNumberException;
import com.dental.management.exception.PatientDeletionException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.repository.AppointmentRepository;
import com.dental.management.repository.PatientRepository;
import com.dental.management.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation managing patient records, uniqueness validations,
 * search functionality, and relational deletion safety.
 */
@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientServiceImpl(PatientRepository patientRepository,
                              AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public PatientResponse createPatient(PatientRequest request) {
        if (patientRepository.existsByPatientNumber(request.getPatientNumber())) {
            throw new DuplicatePatientNumberException("Patient number '" + request.getPatientNumber() + "' is already registered");
        }

        Patient patient = new Patient(
                request.getPatientNumber(),
                request.getFullName(),
                request.getDateOfBirth(),
                request.getGender(),
                request.getPhone(),
                request.getEmail(),
                request.getAddress()
        );

        Patient saved = patientRepository.save(patient);
        return PatientResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return PatientResponse.fromEntity(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(PatientResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPatients();
        }

        return patientRepository.searchPatients(keyword.trim())
                .stream()
                .map(PatientResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        // Enforce uniqueness only if the patientNumber is being altered
        if (!existing.getPatientNumber().equalsIgnoreCase(request.getPatientNumber())
                && patientRepository.existsByPatientNumber(request.getPatientNumber())) {
            throw new DuplicatePatientNumberException("Patient number '" + request.getPatientNumber() + "' is already in use by another patient");
        }

        existing.setPatientNumber(request.getPatientNumber());
        existing.setFullName(request.getFullName());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setGender(request.getGender());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        existing.setAddress(request.getAddress());

        Patient saved = patientRepository.save(existing);
        return PatientResponse.fromEntity(saved);
    }

    @Override
    public void deletePatient(Long id) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        // Relational safety: prevent deleting patients with existing appointment records
        if (!appointmentRepository.findByPatientId(id).isEmpty()) {
            throw new PatientDeletionException("Cannot delete patient '" + existing.getFullName() + "' (ID: " + id + ") because associated appointments exist in the system.");
        }

        patientRepository.delete(existing);
    }
}
