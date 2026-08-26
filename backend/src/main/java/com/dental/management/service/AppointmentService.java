package com.dental.management.service;

import com.dental.management.dto.*;
import com.dental.management.entity.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface defining business operations for Appointment Management
 * and Dentist Availability checking.
 */
public interface AppointmentService {

    /**
     * Book a new appointment after performing entity and conflict validations.
     *
     * @param request appointment details
     * @return created appointment representation
     */
    AppointmentResponse createAppointment(AppointmentRequest request);

    /**
     * Retrieve an appointment by its unique identifier.
     *
     * @param id appointment ID
     * @return appointment representation
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Retrieve appointments matching optional filters (patient, dentist, date, status).
     *
     * @param patientId optional patient ID
     * @param dentistId optional dentist ID
     * @param date      optional appointment date
     * @param status    optional appointment status
     * @return list of matching appointment representations
     */
    List<AppointmentResponse> getAppointments(Long patientId, Long dentistId, LocalDate date, AppointmentStatus status);

    /**
     * Update an existing appointment with conflict checking on date/time/dentist changes.
     *
     * @param id      appointment ID
     * @param request updated appointment details
     * @return updated appointment representation
     */
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

    /**
     * Safely delete an appointment if no billing records exist.
     *
     * @param id appointment ID
     */
    void deleteAppointment(Long id);

    /**
     * Check if a dentist is available for booking at the requested date and time.
     *
     * @param dentistId dentist ID
     * @param date      appointment date
     * @param time      appointment time
     * @return availability response
     */
    AvailabilityResponse checkAvailability(Long dentistId, LocalDate date, LocalTime time);

    /**
     * Retrieve active dentists for appointment booking selection.
     *
     * @return list of active dentists
     */
    List<DentistResponse> getActiveDentists();

    /**
     * Retrieve active treatments for appointment booking selection.
     *
     * @return list of active treatments
     */
    List<TreatmentResponse> getActiveTreatments();
}
