package com.dental.management.service.impl;

import com.dental.management.dto.DentistRequest;
import com.dental.management.dto.DentistResponse;
import com.dental.management.entity.Dentist;
import com.dental.management.exception.DentistDeletionException;
import com.dental.management.exception.DuplicateDentistNumberException;
import com.dental.management.exception.ResourceNotFoundException;
import com.dental.management.repository.AppointmentRepository;
import com.dental.management.repository.DentistRepository;
import com.dental.management.service.DentistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation managing dentist profiles, uniqueness validations,
 * search functionality, and relational deletion safety.
 */
@Service
@Transactional
public class DentistServiceImpl implements DentistService {

    private final DentistRepository dentistRepository;
    private final AppointmentRepository appointmentRepository;

    public DentistServiceImpl(DentistRepository dentistRepository,
                              AppointmentRepository appointmentRepository) {
        this.dentistRepository = dentistRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public DentistResponse createDentist(DentistRequest request) {
        if (dentistRepository.existsByDentistNumber(request.getDentistNumber())) {
            throw new DuplicateDentistNumberException("Dentist number '" + request.getDentistNumber() + "' is already registered");
        }

        Dentist dentist = new Dentist(
                request.getDentistNumber(),
                request.getFullName(),
                request.getSpecialization(),
                request.getPhone(),
                request.getEmail()
        );
        dentist.setActive(request.isActive());

        Dentist saved = dentistRepository.save(dentist);
        return DentistResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponse getDentistById(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
        return DentistResponse.fromEntity(dentist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> getAllDentists() {
        return dentistRepository.findAll()
                .stream()
                .map(DentistResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> searchDentists(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDentists();
        }

        return dentistRepository.searchDentists(keyword.trim())
                .stream()
                .map(DentistResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DentistResponse updateDentist(Long id, DentistRequest request) {
        Dentist existing = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

        // Enforce uniqueness only if the dentistNumber is being altered
        if (!existing.getDentistNumber().equalsIgnoreCase(request.getDentistNumber())
                && dentistRepository.existsByDentistNumber(request.getDentistNumber())) {
            throw new DuplicateDentistNumberException("Dentist number '" + request.getDentistNumber() + "' is already in use by another practitioner");
        }

        existing.setDentistNumber(request.getDentistNumber());
        existing.setFullName(request.getFullName());
        existing.setSpecialization(request.getSpecialization());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        existing.setActive(request.isActive());

        Dentist saved = dentistRepository.save(existing);
        return DentistResponse.fromEntity(saved);
    }

    @Override
    public void deleteDentist(Long id) {
        Dentist existing = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

        // Relational safety: prevent deleting dentists with existing appointment records
        if (!appointmentRepository.findByDentistId(id).isEmpty()) {
            throw new DentistDeletionException("Cannot delete dentist '" + existing.getFullName() + "' (ID: " + id + ") because associated appointments exist in the system.");
        }

        dentistRepository.delete(existing);
    }
}
