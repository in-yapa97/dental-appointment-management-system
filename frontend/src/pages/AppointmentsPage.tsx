import React, { useState, useEffect, useCallback } from 'react';
import {
  Appointment,
  AppointmentRequest,
  AppointmentStatus,
  DentistLookup,
  Patient,
  TreatmentLookup,
} from '../types';
import { appointmentService, AppointmentFilters } from '../services/appointmentService';
import { patientService } from '../services/patientService';
import {
  CalendarCheck,
  Calendar,
  RotateCcw,
  CheckCircle2,
  AlertCircle,
  Eye,
  Edit3,
  Trash2,
} from 'lucide-react';

export const AppointmentsPage: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [dentists, setDentists] = useState<DentistLookup[]>([]);
  const [treatments, setTreatments] = useState<TreatmentLookup[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  // Filters
  const [filters, setFilters] = useState<AppointmentFilters>({
    patientId: undefined,
    dentistId: undefined,
    date: '',
    status: undefined,
  });

  // Modals state
  const [showFormModal, setShowFormModal] = useState<boolean>(false);
  const [editingAppointment, setEditingAppointment] = useState<Appointment | null>(null);
  const [viewingAppointment, setViewingAppointment] = useState<Appointment | null>(null);
  const [deletingAppointment, setDeletingAppointment] = useState<Appointment | null>(null);

  // Booking Form State
  const [formData, setFormData] = useState<AppointmentRequest>({
    patientId: 0,
    dentistId: 0,
    treatmentId: 0,
    appointmentDate: '',
    appointmentTime: '09:00',
    status: 'SCHEDULED',
    notes: '',
  });
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Availability checking state
  const [availabilityResult, setAvailabilityResult] = useState<{ checked: boolean; available: boolean; message: string } | null>(null);
  const [checkingAvailability, setCheckingAvailability] = useState<boolean>(false);

  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await appointmentService.getAppointments(filters);
      setAppointments(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load appointments');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchAppointments();
  }, [fetchAppointments]);

  // Load dropdown resources once
  useEffect(() => {
    Promise.all([
      patientService.getPatients().catch(() => []),
      appointmentService.getActiveDentists().catch(() => []),
      appointmentService.getActiveTreatments().catch(() => []),
    ]).then(([patientList, dentistList, treatmentList]) => {
      setPatients(patientList);
      setDentists(dentistList);
      setTreatments(treatmentList);
    });
  }, []);

  const handleFilterChange = (key: keyof AppointmentFilters, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value === '' ? undefined : value,
    }));
  };

  const handleClearFilters = () => {
    setFilters({
      patientId: undefined,
      dentistId: undefined,
      date: '',
      status: undefined,
    });
  };

  const openBookModal = () => {
    setEditingAppointment(null);
    const todayStr = new Date().toISOString().split('T')[0];
    setFormData({
      patientId: patients.length > 0 ? patients[0].id : 0,
      dentistId: dentists.length > 0 ? dentists[0].id : 0,
      treatmentId: treatments.length > 0 ? treatments[0].id : 0,
      appointmentDate: todayStr,
      appointmentTime: '09:00',
      status: 'SCHEDULED',
      notes: '',
    });
    setAvailabilityResult(null);
    setFormError(null);
    setShowFormModal(true);
  };

  const openEditModal = (apt: Appointment) => {
    setEditingAppointment(apt);
    setFormData({
      patientId: apt.patientId,
      dentistId: apt.dentistId,
      treatmentId: apt.treatmentId,
      appointmentDate: apt.appointmentDate,
      appointmentTime: apt.appointmentTime,
      status: apt.status,
      notes: apt.notes || '',
    });
    setAvailabilityResult(null);
    setFormError(null);
    setShowFormModal(true);
  };

  const handleCheckAvailability = async () => {
    if (!formData.dentistId || !formData.appointmentDate || !formData.appointmentTime) {
      setFormError('Please select a dentist, date, and time slot to verify availability');
      return;
    }
    setCheckingAvailability(true);
    setFormError(null);
    try {
      const isAvailable = await appointmentService.checkAvailability(
        formData.dentistId,
        formData.appointmentDate,
        formData.appointmentTime
      );
      if (isAvailable) {
        setAvailabilityResult({
          checked: true,
          available: true,
          message: 'Selected time slot is OPEN and available for booking.',
        });
      } else {
        setAvailabilityResult({
          checked: true,
          available: false,
          message: 'Selected dentist already has a confirmed appointment at this time.',
        });
      }
    } catch (err: unknown) {
      setAvailabilityResult({
        checked: true,
        available: false,
        message: err instanceof Error ? err.message : 'Failed to verify slot availability',
      });
    } finally {
      setCheckingAvailability(false);
    }
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.patientId || formData.patientId === 0) {
      setFormError('Please select a patient');
      return;
    }
    if (!formData.dentistId || formData.dentistId === 0) {
      setFormError('Please select a dentist');
      return;
    }
    if (!formData.treatmentId || formData.treatmentId === 0) {
      setFormError('Please select a treatment procedure');
      return;
    }
    if (!formData.appointmentDate) {
      setFormError('Appointment date is required');
      return;
    }
    if (!formData.appointmentTime) {
      setFormError('Appointment time is required');
      return;
    }

    setSubmitting(true);
    try {
      const payload: AppointmentRequest = {
        patientId: Number(formData.patientId),
        dentistId: Number(formData.dentistId),
        treatmentId: Number(formData.treatmentId),
        appointmentDate: formData.appointmentDate,
        appointmentTime: formData.appointmentTime,
        status: formData.status,
        notes: formData.notes?.trim() || undefined,
      };

      if (editingAppointment) {
        await appointmentService.updateAppointment(editingAppointment.id, payload);
        setNotice(`Appointment #${editingAppointment.appointmentNumber} updated successfully!`);
      } else {
        const created = await appointmentService.createAppointment(payload);
        setNotice(`Appointment #${created.appointmentNumber} scheduled successfully!`);
      }

      setShowFormModal(false);
      fetchAppointments();
    } catch (err: unknown) {
      setFormError(err instanceof Error ? err.message : 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingAppointment) return;
    setSubmitting(true);
    setError(null);
    try {
      await appointmentService.deleteAppointment(deletingAppointment.id);
      setNotice(`Appointment #${deletingAppointment.appointmentNumber} deleted successfully.`);
      setDeletingAppointment(null);
      fetchAppointments();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete appointment');
      setDeletingAppointment(null);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="patients-container">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Appointment Calendar</h1>
          <p className="page-subtitle">Schedule procedures, manage bookings, and verify dentist availability</p>
        </div>
        <button
          className="btn-primary"
          onClick={openBookModal}
          id="btn-book-appointment"
        >
          <CalendarCheck size={16} />
          <span>Book Appointment</span>
        </button>
      </div>

      {/* Notifications */}
      {notice && (
        <div className="alert-box alert-success banner-notice">
          <CheckCircle2 size={18} className="alert-icon" />
          <span>{notice}</span>
          <button className="close-notice-btn" onClick={() => setNotice(null)}>&times;</button>
        </div>
      )}

      {error && (
        <div className="alert-box alert-error banner-notice">
          <AlertCircle size={18} className="alert-icon" />
          <span>{error}</span>
          <button className="close-notice-btn" onClick={() => setError(null)}>&times;</button>
        </div>
      )}

      {/* Filter Bar */}
      <div className="filter-bar-card">
        <div className="filter-item">
          <label className="filter-label">Patient</label>
          <select
            className="form-input filter-select"
            value={filters.patientId || ''}
            onChange={(e) => handleFilterChange('patientId', e.target.value ? Number(e.target.value) : undefined)}
            id="filter-patient"
          >
            <option value="">All Patients</option>
            {patients.map((p) => (
              <option key={p.id} value={p.id}>{p.fullName} ({p.patientNumber})</option>
            ))}
          </select>
        </div>

        <div className="filter-item">
          <label className="filter-label">Dentist</label>
          <select
            className="form-input filter-select"
            value={filters.dentistId || ''}
            onChange={(e) => handleFilterChange('dentistId', e.target.value ? Number(e.target.value) : undefined)}
            id="filter-dentist"
          >
            <option value="">All Dentists</option>
            {dentists.map((d) => (
              <option key={d.id} value={d.id}>{d.fullName} ({d.specialization})</option>
            ))}
          </select>
        </div>

        <div className="filter-item">
          <label className="filter-label">Date</label>
          <input
            type="date"
            className="form-input filter-select"
            value={filters.date || ''}
            onChange={(e) => handleFilterChange('date', e.target.value)}
            id="filter-date"
          />
        </div>

        <div className="filter-item">
          <label className="filter-label">Status</label>
          <select
            className="form-input filter-select"
            value={filters.status || ''}
            onChange={(e) => handleFilterChange('status', e.target.value as AppointmentStatus)}
            id="filter-status"
          >
            <option value="">All Statuses</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="NO_SHOW">No Show</option>
          </select>
        </div>

        <div className="filter-actions">
          <button
            className="btn-secondary btn-sm"
            onClick={handleClearFilters}
            id="btn-clear-filters"
          >
            <RotateCcw size={14} />
            <span>Reset</span>
          </button>
        </div>
      </div>

      {/* Appointments Table */}
      <div className="table-wrapper">
        {loading ? (
          <div className="table-loading">
            <div className="spinner"></div>
            <span>Loading appointments...</span>
          </div>
        ) : appointments.length === 0 ? (
          <div className="empty-state">
            <Calendar size={42} style={{ color: '#94a3b8', marginBottom: '0.75rem' }} />
            <h3>No Appointments Found</h3>
            <p>No appointments match the selected filter criteria. Click "+ Book Appointment" to schedule one.</p>
          </div>
        ) : (
          <table className="data-table patients-table">
            <thead>
              <tr>
                <th>Appt #</th>
                <th>Patient</th>
                <th>Dentist</th>
                <th>Treatment</th>
                <th>Date &amp; Time</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id} id={`appointment-row-${a.id}`}>
                  <td>
                    <span className="patient-number-badge">{a.appointmentNumber}</span>
                  </td>
                  <td>
                    <div className="patient-name-cell">
                      <span className="cell-fullname">{a.patientName}</span>
                      <span className="cell-subtext">{a.patientNumber}</span>
                    </div>
                  </td>
                  <td>
                    <div className="patient-name-cell">
                      <span className="cell-fullname">{a.dentistName}</span>
                      <span className="cell-subtext">{a.dentistNumber}</span>
                    </div>
                  </td>
                  <td>
                    <div className="patient-name-cell">
                      <span className="cell-fullname">{a.treatmentName}</span>
                      <span className="cell-subtext">${Number(a.treatmentCost).toFixed(2)}</span>
                    </div>
                  </td>
                  <td>
                    <div className="patient-meta-cell">
                      <strong>{a.appointmentDate}</strong>
                      <span className="cell-subtext">{a.appointmentTime}</span>
                    </div>
                  </td>
                  <td>
                    <span className={`status-badge status-${a.status.toLowerCase()}`}>
                      {a.status}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="table-actions" style={{ justifyContent: 'flex-end', gap: '0.35rem' }}>
                      <button
                        className="action-btn action-view"
                        onClick={() => setViewingAppointment(a)}
                        title="View Details"
                        id={`btn-view-apt-${a.id}`}
                      >
                        <Eye size={14} />
                      </button>
                      <button
                        className="action-btn action-edit"
                        onClick={() => openEditModal(a)}
                        title="Edit Appointment"
                        id={`btn-edit-apt-${a.id}`}
                      >
                        <Edit3 size={14} />
                      </button>
                      <button
                        className="action-btn action-delete"
                        onClick={() => setDeletingAppointment(a)}
                        title="Delete / Cancel Appointment"
                        id={`btn-delete-apt-${a.id}`}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* BOOK / EDIT MODAL */}
      {showFormModal && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card">
            <div className="modal-header">
              <h2 className="auth-title">
                {editingAppointment ? `Edit Appointment #${editingAppointment.appointmentNumber}` : 'Book New Appointment'}
              </h2>
              <button className="modal-close" onClick={() => setShowFormModal(false)}>&times;</button>
            </div>

            {formError && (
              <div className="alert-box alert-error" style={{ marginBottom: '1rem' }}>
                <span className="alert-icon">&#9888;</span>
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleFormSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Patient *</label>
                <select
                  className="form-input"
                  value={formData.patientId}
                  onChange={(e) => setFormData({ ...formData, patientId: Number(e.target.value) })}
                  disabled={submitting}
                >
                  <option value={0}>-- Select Patient --</option>
                  {patients.map((p) => (
                    <option key={p.id} value={p.id}>{p.fullName} ({p.patientNumber})</option>
                  ))}
                </select>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Dentist *</label>
                  <select
                    className="form-input"
                    value={formData.dentistId}
                    onChange={(e) => {
                      setFormData({ ...formData, dentistId: Number(e.target.value) });
                      setAvailabilityResult(null);
                    }}
                    disabled={submitting}
                  >
                    <option value={0}>-- Select Dentist --</option>
                    {dentists.map((d) => (
                      <option key={d.id} value={d.id}>{d.fullName} ({d.specialization})</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Treatment Procedure *</label>
                  <select
                    className="form-input"
                    value={formData.treatmentId}
                    onChange={(e) => setFormData({ ...formData, treatmentId: Number(e.target.value) })}
                    disabled={submitting}
                  >
                    <option value={0}>-- Select Procedure --</option>
                    {treatments.map((t) => (
                      <option key={t.id} value={t.id}>{t.treatmentName} (${Number(t.cost).toFixed(2)})</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Appointment Date *</label>
                  <input
                    type="date"
                    className="form-input"
                    value={formData.appointmentDate}
                    onChange={(e) => {
                      setFormData({ ...formData, appointmentDate: e.target.value });
                      setAvailabilityResult(null);
                    }}
                    disabled={submitting}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Appointment Time *</label>
                  <input
                    type="time"
                    className="form-input"
                    value={formData.appointmentTime}
                    onChange={(e) => {
                      setFormData({ ...formData, appointmentTime: e.target.value });
                      setAvailabilityResult(null);
                    }}
                    disabled={submitting}
                  />
                </div>
              </div>

              {/* Dentist Availability Feedback Bar */}
              <div className="availability-check-row">
                <button
                  type="button"
                  className="check-btn"
                  style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem' }}
                  onClick={handleCheckAvailability}
                  disabled={checkingAvailability || !formData.dentistId || !formData.appointmentDate}
                  id="btn-check-availability"
                >
                  {checkingAvailability ? 'Checking...' : 'Check Dentist Availability'}
                </button>

                {availabilityResult && (
                  <span className={`availability-tag ${availabilityResult.available ? 'available' : 'unavailable'}`}>
                    {availabilityResult.available ? '✓ Slot Available' : `✗ ${availabilityResult.message}`}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Status</label>
                <select
                  className="form-input"
                  value={formData.status || 'SCHEDULED'}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as AppointmentStatus })}
                  disabled={submitting}
                >
                  <option value="SCHEDULED">Scheduled</option>
                  <option value="CONFIRMED">Confirmed</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                  <option value="NO_SHOW">No Show</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Clinical / Appointment Notes (Optional)</label>
                <textarea
                  className="form-input"
                  rows={2}
                  placeholder="Special instructions, medical alerts, or visit reasons"
                  value={formData.notes || ''}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  disabled={submitting}
                />
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="nav-btn"
                  onClick={() => setShowFormModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="submit-btn"
                  disabled={submitting}
                  id="btn-save-appointment"
                  style={{ width: 'auto', padding: '0.65rem 1.5rem' }}
                >
                  {submitting ? 'Saving...' : editingAppointment ? 'Update Appointment' : 'Confirm Booking'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* VIEW DETAILS MODAL */}
      {viewingAppointment && (
        <div className="modal-overlay">
          <div className="modal-content profile-card modal-card">
            <div className="modal-header">
              <h2 className="profile-fullname">Appointment #{viewingAppointment.appointmentNumber}</h2>
              <button className="modal-close" onClick={() => setViewingAppointment(null)}>&times;</button>
            </div>

            <div className="profile-details-grid" style={{ paddingTop: '0.5rem' }}>
              <div className="detail-item">
                <span className="detail-label">Status</span>
                <span className={`status-badge status-${viewingAppointment.status.toLowerCase()}`}>
                  {viewingAppointment.status}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Date &amp; Time</span>
                <span className="detail-value">{viewingAppointment.appointmentDate} at {viewingAppointment.appointmentTime}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Patient</span>
                <span className="detail-value">{viewingAppointment.patientName} ({viewingAppointment.patientNumber})</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Attending Dentist</span>
                <span className="detail-value">{viewingAppointment.dentistName} ({viewingAppointment.dentistNumber})</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Procedure / Treatment</span>
                <span className="detail-value">{viewingAppointment.treatmentName} ({viewingAppointment.treatmentCode})</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Estimated Cost</span>
                <span className="detail-value">${Number(viewingAppointment.treatmentCost).toFixed(2)}</span>
              </div>
              <div className="detail-item" style={{ gridColumn: 'span 2' }}>
                <span className="detail-label">Notes</span>
                <span className="detail-value">{viewingAppointment.notes || 'None recorded'}</span>
              </div>
              <div className="detail-item" style={{ gridColumn: 'span 2' }}>
                <span className="detail-label">Booking Timestamp</span>
                <span className="detail-value">
                  {viewingAppointment.createdAt ? new Date(viewingAppointment.createdAt).toLocaleString() : 'N/A'}
                </span>
              </div>
            </div>

            <div className="profile-actions" style={{ justifyContent: 'flex-end' }}>
              <button
                className="check-btn"
                onClick={() => {
                  const toEdit = viewingAppointment;
                  setViewingAppointment(null);
                  openEditModal(toEdit);
                }}
              >
                Edit Details
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE / CANCEL MODAL */}
      {deletingAppointment && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card" style={{ maxWidth: '480px' }}>
            <div className="modal-header">
              <h2 className="auth-title" style={{ color: '#dc2626' }}>Cancel / Delete Appointment</h2>
              <button className="modal-close" onClick={() => setDeletingAppointment(null)}>&times;</button>
            </div>

            <p style={{ color: 'var(--text-main)', fontSize: '0.95rem', margin: '1rem 0' }}>
              Are you sure you want to remove appointment{' '}
              <strong>#{deletingAppointment.appointmentNumber}</strong> for{' '}
              <strong>{deletingAppointment.patientName}</strong> on {deletingAppointment.appointmentDate}?
            </p>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Billing Safety Rule: Appointments with existing billing records cannot be removed to preserve audit trails.
            </p>

            <div className="modal-actions">
              <button
                type="button"
                className="nav-btn"
                onClick={() => setDeletingAppointment(null)}
                disabled={submitting}
              >
                Keep Appointment
              </button>
              <button
                type="button"
                className="logout-btn"
                style={{ backgroundColor: '#dc2626', color: 'white', borderColor: '#dc2626' }}
                onClick={handleDeleteConfirm}
                disabled={submitting}
                id="btn-confirm-delete-apt"
              >
                {submitting ? 'Deleting...' : 'Delete Appointment'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
