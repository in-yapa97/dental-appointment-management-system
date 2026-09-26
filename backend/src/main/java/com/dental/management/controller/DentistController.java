package com.dental.management.controller;

import com.dental.management.dto.DentistRequest;
import com.dental.management.dto.DentistResponse;
import com.dental.management.dto.MessageResponse;
import com.dental.management.service.DentistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller providing dentist CRUD operations, search functionality,
 * and practitioner lifecycle management.
 * Requires JWT authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/v1/dentists")
public class DentistController {

    private final DentistService dentistService;

    public DentistController(DentistService dentistService) {
        this.dentistService = dentistService;
    }

    /**
     * Create a new dentist record.
     *
     * @param request creation payload
     * @return 201 CREATED with created dentist representation
     */
    @PostMapping
    public ResponseEntity<DentistResponse> createDentist(@Valid @RequestBody DentistRequest request) {
        DentistResponse created = dentistService.createDentist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieve all dentist records.
     *
     * @return 200 OK with list of dentists
     */
    @GetMapping
    public ResponseEntity<List<DentistResponse>> getAllDentists() {
        List<DentistResponse> dentists = dentistService.getAllDentists();
        return ResponseEntity.ok(dentists);
    }

    /**
     * Search dentists by keyword across number, name, specialization, phone, or email.
     *
     * @param keyword search keyword
     * @return 200 OK with list of matching dentists
     */
    @GetMapping("/search")
    public ResponseEntity<List<DentistResponse>> searchDentists(@RequestParam(name = "keyword", required = false) String keyword) {
        List<DentistResponse> results = dentistService.searchDentists(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Retrieve dentist details by unique ID.
     *
     * @param id dentist ID
     * @return 200 OK with dentist representation
     */
    @GetMapping("/{id}")
    public ResponseEntity<DentistResponse> getDentistById(@PathVariable Long id) {
        DentistResponse dentist = dentistService.getDentistById(id);
        return ResponseEntity.ok(dentist);
    }

    /**
     * Update an existing dentist record.
     *
     * @param id      dentist ID
     * @param request update payload
     * @return 200 OK with updated dentist representation
     */
    @PutMapping("/{id}")
    public ResponseEntity<DentistResponse> updateDentist(@PathVariable Long id,
                                                         @Valid @RequestBody DentistRequest request) {
        DentistResponse updated = dentistService.updateDentist(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a dentist record if no associated appointments exist.
     *
     * @param id dentist ID
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDentist(@PathVariable Long id) {
        dentistService.deleteDentist(id);
        return ResponseEntity.ok(new MessageResponse("Dentist deleted successfully"));
    }
}
