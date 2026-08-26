package com.dental.management.controller;

import com.dental.management.dto.MessageResponse;
import com.dental.management.dto.PatientRequest;
import com.dental.management.dto.PatientResponse;
import com.dental.management.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller providing patient CRUD operations and search functionality.
 * Requires authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Create a new patient record.
     *
     * @param request creation payload
     * @return 201 CREATED with created patient representation
     */
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse created = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieve all patient records.
     *
     * @return 200 OK with list of patients
     */
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Search patients by keyword across patient number, name, phone, or email.
     *
     * @param keyword search keyword
     * @return 200 OK with list of matching patients (empty list if none)
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> searchPatients(@RequestParam(name = "keyword", required = false) String keyword) {
        List<PatientResponse> results = patientService.searchPatients(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Retrieve patient details by unique ID.
     *
     * @param id patient ID
     * @return 200 OK with patient representation
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Update an existing patient record.
     *
     * @param id      patient ID
     * @param request update payload
     * @return 200 OK with updated patient representation
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id,
                                                         @Valid @RequestBody PatientRequest request) {
        PatientResponse updated = patientService.updatePatient(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a patient record if no associated appointments exist.
     *
     * @param id patient ID
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(new MessageResponse("Patient deleted successfully"));
    }
}
