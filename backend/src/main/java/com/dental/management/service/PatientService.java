package com.dental.management.service;

import com.dental.management.dto.PatientRequest;
import com.dental.management.dto.PatientResponse;

import java.util.List;

/**
 * Service interface defining business operations for Patient Management.
 */
public interface PatientService {

    /**
     * Create and persist a new patient.
     *
     * @param request patient creation details
     * @return created patient response
     */
    PatientResponse createPatient(PatientRequest request);

    /**
     * Retrieve patient details by unique ID.
     *
     * @param id patient ID
     * @return patient response
     */
    PatientResponse getPatientById(Long id);

    /**
     * Retrieve all patient records.
     *
     * @return list of patient responses
     */
    List<PatientResponse> getAllPatients();

    /**
     * Search patients by keyword across patient number, name, phone, or email.
     *
     * @param keyword search keyword
     * @return list of matching patient responses
     */
    List<PatientResponse> searchPatients(String keyword);

    /**
     * Update an existing patient record.
     *
     * @param id      patient ID
     * @param request updated patient details
     * @return updated patient response
     */
    PatientResponse updatePatient(Long id, PatientRequest request);

    /**
     * Safely delete a patient if no associated records (appointments) exist.
     *
     * @param id patient ID
     */
    void deletePatient(Long id);
}
