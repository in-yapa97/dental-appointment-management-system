package com.dental.management.controller;

import com.dental.management.dto.*;
import com.dental.management.entity.enums.AppointmentStatus;
import com.dental.management.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * REST controller providing appointment booking, filtering, updates, safe deletion,
 * and dentist availability checking.
 * Requires JWT authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Book a new appointment.
     *
     * @param request appointment details
     * @return 201 CREATED with created appointment representation
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse created = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieve appointments with optional filters for patient, dentist, date, and status.
     *
     * @param patientId optional patient ID
     * @param dentistId optional dentist ID
     * @param date      optional appointment date
     * @param status    optional appointment status
     * @return 200 OK with list of matching appointments
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @RequestParam(name = "patientId", required = false) Long patientId,
            @RequestParam(name = "dentistId", required = false) Long dentistId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "status", required = false) AppointmentStatus status) {
        List<AppointmentResponse> results = appointmentService.getAppointments(patientId, dentistId, date, status);
        return ResponseEntity.ok(results);
    }

    /**
     * Retrieve appointment by ID.
     *
     * @param id appointment ID
     * @return 200 OK with appointment representation
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Update an existing appointment.
     *
     * @param id      appointment ID
     * @param request updated appointment details
     * @return 200 OK with updated appointment representation
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse updated = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Safely delete an appointment if no billing records are attached.
     *
     * @param id appointment ID
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new MessageResponse("Appointment deleted successfully"));
    }

    /**
     * Check if a dentist is available for booking at a specific date and time.
     *
     * @param dentistId dentist ID
     * @param date      appointment date
     * @param time      appointment time
     * @return 200 OK with availability response
     */
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam(name = "dentistId") Long dentistId,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        AvailabilityResponse response = appointmentService.checkAvailability(dentistId, date, time);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve active dentists for appointment booking selection.
     *
     * @return 200 OK with list of active dentists
     */
    @GetMapping("/dentists")
    public ResponseEntity<List<DentistResponse>> getActiveDentists() {
        List<DentistResponse> dentists = appointmentService.getActiveDentists();
        return ResponseEntity.ok(dentists);
    }

    /**
     * Retrieve active treatments for appointment booking selection.
     *
     * @return 200 OK with list of active treatments
     */
    @GetMapping("/treatments")
    public ResponseEntity<List<TreatmentResponse>> getActiveTreatments() {
        List<TreatmentResponse> treatments = appointmentService.getActiveTreatments();
        return ResponseEntity.ok(treatments);
    }
}
