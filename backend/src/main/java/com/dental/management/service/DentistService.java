package com.dental.management.service;

import com.dental.management.dto.DentistRequest;
import com.dental.management.dto.DentistResponse;

import java.util.List;

/**
 * Service interface for Dentist Management CRUD and search operations.
 */
public interface DentistService {

    /**
     * Create a new dentist profile.
     *
     * @param request dentist payload
     * @return created dentist representation
     */
    DentistResponse createDentist(DentistRequest request);

    /**
     * Retrieve dentist details by unique ID.
     *
     * @param id dentist ID
     * @return dentist representation
     */
    DentistResponse getDentistById(Long id);

    /**
     * Retrieve all dentists.
     *
     * @return list of dentists
     */
    List<DentistResponse> getAllDentists();

    /**
     * Search dentists by keyword across number, name, specialization, phone, or email.
     *
     * @param keyword search keyword
     * @return list of matching dentists
     */
    List<DentistResponse> searchDentists(String keyword);

    /**
     * Update an existing dentist profile.
     *
     * @param id      dentist ID
     * @param request update payload
     * @return updated dentist representation
     */
    DentistResponse updateDentist(Long id, DentistRequest request);

    /**
     * Delete a dentist profile if no appointments are attached.
     *
     * @param id dentist ID
     */
    void deleteDentist(Long id);
}
