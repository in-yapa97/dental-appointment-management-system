import React, { useState, useEffect, useCallback } from 'react';
import { Gender, Patient, PatientRequest } from '../types';
import { patientService } from '../services/patientService';
import {
  Search,
  X,
  UserPlus,
  Eye,
  Edit3,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Users,
} from 'lucide-react';

export const PatientsPage: React.FC = () => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  // Modals state
  const [showFormModal, setShowFormModal] = useState<boolean>(false);
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null);
  const [viewingPatient, setViewingPatient] = useState<Patient | null>(null);
  const [deletingPatient, setDeletingPatient] = useState<Patient | null>(null);

  // Form fields
  const [formData, setFormData] = useState<PatientRequest>({
    patientNumber: '',
    fullName: '',
    dateOfBirth: '',
    gender: 'MALE',
    phone: '',
    email: '',
    address: '',
  });
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  const fetchPatients = useCallback(async (keyword = '') => {
    setLoading(true);
    setError(null);
    try {
      const data = await patientService.searchPatients(keyword);
      setPatients(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load patients');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPatients();
  }, [fetchPatients]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setSearchKeyword(val);
    fetchPatients(val);
  };

  const handleClearSearch = () => {
    setSearchKeyword('');
    fetchPatients('');
  };

  const openCreateModal = () => {
    setEditingPatient(null);
    setFormData({
      patientNumber: '',
      fullName: '',
      dateOfBirth: '',
      gender: 'MALE',
      phone: '',
      email: '',
      address: '',
    });
    setFormError(null);
    setShowFormModal(true);
  };

  const openEditModal = (patient: Patient) => {
    setEditingPatient(patient);
    setFormData({
      patientNumber: patient.patientNumber,
      fullName: patient.fullName,
      dateOfBirth: patient.dateOfBirth,
      gender: patient.gender,
      phone: patient.phone,
      email: patient.email || '',
      address: patient.address || '',
    });
    setFormError(null);
    setShowFormModal(true);
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    // Basic Client Validations
    if (!formData.patientNumber.trim()) {
      setFormError('Patient number is required');
      return;
    }
    if (!formData.fullName.trim()) {
      setFormError('Full name is required');
      return;
    }
    if (!formData.dateOfBirth) {
      setFormError('Date of birth is required');
      return;
    }
    if (new Date(formData.dateOfBirth) >= new Date()) {
      setFormError('Date of birth must be in the past');
      return;
    }
    if (!formData.phone.trim()) {
      setFormError('Phone number is required');
      return;
    }

    setSubmitting(true);
    try {
      const payload: PatientRequest = {
        patientNumber: formData.patientNumber.trim(),
        fullName: formData.fullName.trim(),
        dateOfBirth: formData.dateOfBirth,
        gender: formData.gender,
        phone: formData.phone.trim(),
        email: formData.email?.trim() || undefined,
        address: formData.address?.trim() || undefined,
      };

      if (editingPatient) {
        await patientService.updatePatient(editingPatient.id, payload);
        setNotice(`Patient #${editingPatient.patientNumber} updated successfully!`);
      } else {
        await patientService.createPatient(payload);
        setNotice(`Patient #${payload.patientNumber} created successfully!`);
      }

      setShowFormModal(false);
      fetchPatients(searchKeyword);
    } catch (err: unknown) {
      setFormError(err instanceof Error ? err.message : 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingPatient) return;
    setSubmitting(true);
    setError(null);
    try {
      await patientService.deletePatient(deletingPatient.id);
      setNotice(`Patient #${deletingPatient.patientNumber} (${deletingPatient.fullName}) deleted successfully.`);
      setDeletingPatient(null);
      fetchPatients(searchKeyword);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete patient');
      setDeletingPatient(null);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="patients-container">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Patient Directory</h1>
          <p className="page-subtitle">Manage patient profiles, medical histories, and contact info</p>
        </div>
        <button
          className="btn-primary"
          onClick={openCreateModal}
          id="btn-add-patient"
        >
          <UserPlus size={16} />
          <span>Register Patient</span>
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

      {/* Search Bar */}
      <div className="search-bar-wrapper" style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
        <Search size={18} style={{ position: 'absolute', left: '1rem', color: '#94a3b8', pointerEvents: 'none' }} />
        <input
          type="text"
          className="form-input search-input"
          style={{ paddingLeft: '2.75rem', paddingRight: '2.5rem' }}
          placeholder="Search by patient #, name, phone, or email..."
          value={searchKeyword}
          onChange={handleSearchChange}
          id="input-search-patients"
        />
        {searchKeyword && (
          <button className="clear-search-btn" onClick={handleClearSearch} title="Clear search">
            <X size={16} />
          </button>
        )}
      </div>

      {/* Patient Table / Content */}
      <div className="table-wrapper">
        {loading ? (
          <div className="table-loading">
            <div className="spinner"></div>
            <span>Loading patient directory...</span>
          </div>
        ) : patients.length === 0 ? (
          <div className="empty-state">
            <Users size={42} style={{ color: '#94a3b8', marginBottom: '0.75rem' }} />
            <h3>No Patients Found</h3>
            <p>
              {searchKeyword
                ? `No patient records match the keyword "${searchKeyword}".`
                : 'No patient records exist yet. Click "+ Register Patient" to add one.'}
            </p>
          </div>
        ) : (
          <table className="data-table patients-table">
            <thead>
              <tr>
                <th>Patient #</th>
                <th>Full Name</th>
                <th>DOB / Gender</th>
                <th>Contact</th>
                <th>Address</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {patients.map((p) => (
                <tr key={p.id} id={`patient-row-${p.id}`}>
                  <td>
                    <span className="patient-number-badge">{p.patientNumber}</span>
                  </td>
                  <td>
                    <div className="patient-name-cell">
                      <span className="cell-fullname">{p.fullName}</span>
                      <span className="cell-subtext">ID #{p.id}</span>
                    </div>
                  </td>
                  <td>
                    <div className="patient-meta-cell">
                      <span>{p.dateOfBirth}</span>
                      <span className={`gender-tag gender-${p.gender.toLowerCase()}`}>
                        {p.gender}
                      </span>
                    </div>
                  </td>
                  <td>
                    <div className="patient-contact-cell">
                      <span className="contact-phone">{p.phone}</span>
                      {p.email && <span className="contact-email">{p.email}</span>}
                    </div>
                  </td>
                  <td>
                    <span className="patient-address-cell">{p.address || '—'}</span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="table-actions" style={{ justifyContent: 'flex-end', gap: '0.35rem' }}>
                      <button
                        className="action-btn action-view"
                        onClick={() => setViewingPatient(p)}
                        title="View Details"
                        id={`btn-view-${p.id}`}
                      >
                        <Eye size={14} />
                      </button>
                      <button
                        className="action-btn action-edit"
                        onClick={() => openEditModal(p)}
                        title="Edit Details"
                        id={`btn-edit-${p.id}`}
                      >
                        <Edit3 size={14} />
                      </button>
                      <button
                        className="action-btn action-delete"
                        onClick={() => setDeletingPatient(p)}
                        title="Delete Patient"
                        id={`btn-delete-${p.id}`}
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

      {/* CREATE / EDIT MODAL */}
      {showFormModal && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card">
            <div className="modal-header">
              <h2 className="auth-title">
                {editingPatient ? `Edit Patient #${editingPatient.patientNumber}` : 'Register New Patient'}
              </h2>
              <button className="modal-close" onClick={() => setShowFormModal(false)}>&times;</button>
            </div>

            {formError && (
              <div className="alert-box alert-error" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={16} />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleFormSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Patient Number *</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. PAT-001"
                  value={formData.patientNumber}
                  onChange={(e) => setFormData({ ...formData, patientNumber: e.target.value })}
                  disabled={submitting}
                  autoFocus
                />
              </div>

              <div className="form-group">
                <label className="form-label">Full Name *</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. John Doe"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  disabled={submitting}
                />
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Date of Birth *</label>
                  <input
                    type="date"
                    className="form-input"
                    value={formData.dateOfBirth}
                    onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                    disabled={submitting}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Gender *</label>
                  <select
                    className="form-input"
                    value={formData.gender}
                    onChange={(e) => setFormData({ ...formData, gender: e.target.value as Gender })}
                    disabled={submitting}
                  >
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Phone Number *</label>
                  <input
                    type="tel"
                    className="form-input"
                    placeholder="e.g. +1-555-0123"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    disabled={submitting}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Email Address (Optional)</label>
                  <input
                    type="email"
                    className="form-input"
                    placeholder="e.g. patient@example.com"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Residential Address (Optional)</label>
                <textarea
                  className="form-input"
                  rows={2}
                  placeholder="e.g. 123 Main Street, Springfield"
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  disabled={submitting}
                />
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowFormModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={submitting}
                  id="btn-save-patient"
                >
                  {submitting ? 'Saving...' : editingPatient ? 'Update Patient' : 'Create Patient'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* VIEW DETAILS MODAL */}
      {viewingPatient && (
        <div className="modal-overlay">
          <div className="modal-content profile-card modal-card">
            <div className="modal-header">
              <h2 className="profile-fullname">Patient #{viewingPatient.patientNumber}</h2>
              <button className="modal-close" onClick={() => setViewingPatient(null)}>&times;</button>
            </div>

            <div className="profile-details-grid" style={{ paddingTop: '0.5rem' }}>
              <div className="detail-item">
                <span className="detail-label">System ID</span>
                <span className="detail-value">#{viewingPatient.id}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Patient Number</span>
                <span className="detail-value">{viewingPatient.patientNumber}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Full Name</span>
                <span className="detail-value">{viewingPatient.fullName}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Gender</span>
                <span className="detail-value">{viewingPatient.gender}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Date of Birth</span>
                <span className="detail-value">{viewingPatient.dateOfBirth}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Phone</span>
                <span className="detail-value">{viewingPatient.phone}</span>
              </div>
              <div className="detail-item" style={{ gridColumn: 'span 2' }}>
                <span className="detail-label">Email Address</span>
                <span className="detail-value">{viewingPatient.email || 'None registered'}</span>
              </div>
              <div className="detail-item" style={{ gridColumn: 'span 2' }}>
                <span className="detail-label">Address</span>
                <span className="detail-value">{viewingPatient.address || 'None registered'}</span>
              </div>
              <div className="detail-item" style={{ gridColumn: 'span 2' }}>
                <span className="detail-label">Registration Timestamp</span>
                <span className="detail-value">
                  {viewingPatient.createdAt ? new Date(viewingPatient.createdAt).toLocaleString() : 'N/A'}
                </span>
              </div>
            </div>

            <div className="profile-actions" style={{ justifyContent: 'flex-end' }}>
              <button
                className="btn-primary"
                onClick={() => {
                  const toEdit = viewingPatient;
                  setViewingPatient(null);
                  openEditModal(toEdit);
                }}
              >
                <Edit3 size={16} />
                <span>Edit Details</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deletingPatient && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card" style={{ maxWidth: '480px' }}>
            <div className="modal-header">
              <h2 className="auth-title" style={{ color: '#ef4444' }}>Confirm Deletion</h2>
              <button className="modal-close" onClick={() => setDeletingPatient(null)}>&times;</button>
            </div>

            <p style={{ color: 'var(--text-main)', fontSize: '0.95rem', margin: '1rem 0' }}>
              Are you sure you want to delete patient record{' '}
              <strong>#{deletingPatient.patientNumber} ({deletingPatient.fullName})</strong>?
            </p>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Relational Safety Rule: Patients with active appointment records cannot be deleted to preserve clinic history.
            </p>

            <div className="modal-actions">
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setDeletingPatient(null)}
                disabled={submitting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn-danger"
                onClick={handleDeleteConfirm}
                disabled={submitting}
                id="btn-confirm-delete"
              >
                {submitting ? 'Deleting...' : 'Delete Patient'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

