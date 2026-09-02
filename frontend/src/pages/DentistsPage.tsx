import React, { useState, useEffect, useCallback } from 'react';
import { Dentist, DentistRequest } from '../types';
import { dentistService } from '../services/dentistService';
import {
  Search,
  X,
  UserPlus,
  Eye,
  Edit3,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Stethoscope,
  Sparkles,
} from 'lucide-react';

const SPECIALIZATIONS = [
  'General Dentistry',
  'Orthodontics',
  'Endodontics',
  'Periodontics',
  'Oral Surgery',
  'Pediatric Dentistry',
  'Prosthodontics',
  'Cosmetic Dentistry',
  'Oral Pathology',
];

export const DentistsPage: React.FC = () => {
  const [dentists, setDentists] = useState<Dentist[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [selectedSpecialization, setSelectedSpecialization] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  // Modals state
  const [showFormModal, setShowFormModal] = useState<boolean>(false);
  const [editingDentist, setEditingDentist] = useState<Dentist | null>(null);
  const [viewingDentist, setViewingDentist] = useState<Dentist | null>(null);
  const [deletingDentist, setDeletingDentist] = useState<Dentist | null>(null);

  // Form fields
  const [formData, setFormData] = useState<DentistRequest>({
    dentistNumber: '',
    fullName: '',
    specialization: 'General Dentistry',
    phone: '',
    email: '',
    active: true,
  });
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  const fetchDentists = useCallback(async (keyword = '') => {
    setLoading(true);
    setError(null);
    try {
      const data = await dentistService.searchDentists(keyword);
      setDentists(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load dentists');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDentists();
  }, [fetchDentists]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setSearchKeyword(val);
    fetchDentists(val);
  };

  const handleClearSearch = () => {
    setSearchKeyword('');
    fetchDentists('');
  };

  const generateDentistNumber = () => {
    const randomSuffix = Math.floor(100 + Math.random() * 900);
    const generated = `DEN-${randomSuffix}`;
    setFormData((prev) => ({ ...prev, dentistNumber: generated }));
  };

  const openCreateModal = () => {
    setEditingDentist(null);
    const randomSuffix = Math.floor(100 + Math.random() * 900);
    setFormData({
      dentistNumber: `DEN-${randomSuffix}`,
      fullName: '',
      specialization: 'General Dentistry',
      phone: '',
      email: '',
      active: true,
    });
    setFormError(null);
    setShowFormModal(true);
  };

  const openEditModal = (dentist: Dentist) => {
    setEditingDentist(dentist);
    setFormData({
      dentistNumber: dentist.dentistNumber,
      fullName: dentist.fullName,
      specialization: dentist.specialization,
      phone: dentist.phone,
      email: dentist.email || '',
      active: dentist.active,
    });
    setFormError(null);
    setShowFormModal(true);
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.dentistNumber.trim()) {
      setFormError('Dentist Number is required.');
      return;
    }
    if (!formData.fullName.trim()) {
      setFormError('Full Name is required.');
      return;
    }
    if (!formData.specialization.trim()) {
      setFormError('Specialization is required.');
      return;
    }
    if (!formData.phone.trim()) {
      setFormError('Phone number is required.');
      return;
    }

    setSubmitting(true);
    try {
      if (editingDentist) {
        await dentistService.updateDentist(editingDentist.id, formData);
        setNotice(`Dentist ${formData.fullName} updated successfully.`);
      } else {
        await dentistService.createDentist(formData);
        setNotice(`Dentist ${formData.fullName} registered successfully.`);
      }
      setShowFormModal(false);
      fetchDentists(searchKeyword);
    } catch (err: unknown) {
      setFormError(err instanceof Error ? err.message : 'Failed to save dentist details.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingDentist) return;
    setSubmitting(true);
    try {
      await dentistService.deleteDentist(deletingDentist.id);
      setNotice(`Dentist ${deletingDentist.fullName} record deleted.`);
      setDeletingDentist(null);
      fetchDentists(searchKeyword);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete dentist.');
      setDeletingDentist(null);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredDentists = selectedSpecialization
    ? dentists.filter((d) => d.specialization === selectedSpecialization)
    : dentists;

  return (
    <div className="patients-container">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Dentist Directory</h1>
          <p className="page-subtitle">Manage licensed dental specialists, specializations, contact details, and practicing status</p>
        </div>
        <button
          className="btn-primary"
          onClick={openCreateModal}
          id="btn-add-dentist"
        >
          <UserPlus size={16} />
          <span>Register Dentist</span>
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

      {/* Search & Filter Bar */}
      <div className="filter-bar-card">
        <div className="filter-item" style={{ flex: '2 1 280px' }}>
          <label className="filter-label">Search Practitioner</label>
          <div className="search-bar-wrapper" style={{ position: 'relative', display: 'flex', alignItems: 'center', marginBottom: 0 }}>
            <Search size={18} style={{ position: 'absolute', left: '1rem', color: '#94a3b8', pointerEvents: 'none' }} />
            <input
              type="text"
              className="form-input search-input"
              style={{ paddingLeft: '2.75rem', paddingRight: '2.5rem' }}
              placeholder="Search by dentist ID (DEN-...), name, specialization, phone, or email..."
              value={searchKeyword}
              onChange={handleSearchChange}
              id="input-search-dentists"
            />
            {searchKeyword && (
              <button className="clear-search-btn" onClick={handleClearSearch} title="Clear search">
                <X size={16} />
              </button>
            )}
          </div>
        </div>

        <div className="filter-item" style={{ flex: '1 1 200px' }}>
          <label className="filter-label">Specialization</label>
          <select
            className="form-input filter-select"
            value={selectedSpecialization}
            onChange={(e) => setSelectedSpecialization(e.target.value)}
            id="select-filter-specialization"
          >
            <option value="">All Specializations</option>
            {SPECIALIZATIONS.map((spec) => (
              <option key={spec} value={spec}>
                {spec}
              </option>
            ))}
          </select>
        </div>

        {(searchKeyword || selectedSpecialization) && (
          <div className="filter-item" style={{ flex: '0 0 auto', alignSelf: 'flex-end' }}>
            <button
              className="btn-secondary"
              onClick={() => {
                handleClearSearch();
                setSelectedSpecialization('');
              }}
              style={{ padding: '0.65rem 1rem' }}
            >
              Clear
            </button>
          </div>
        )}
      </div>

      {/* Dentists Data Table */}
      <div className="table-wrapper">
        {loading ? (
          <div className="table-loading">
            <div className="spinner"></div>
            <span>Loading practitioner directory...</span>
          </div>
        ) : filteredDentists.length === 0 ? (
          <div className="empty-state">
            <Stethoscope size={42} style={{ color: '#94a3b8', marginBottom: '0.75rem' }} />
            <h3>No Dentists Found</h3>
            <p>
              {searchKeyword || selectedSpecialization
                ? 'No practitioners match your search criteria. Try clearing filters.'
                : 'No dental practitioners registered yet. Click "+ Register Dentist" to add one.'}
            </p>
          </div>
        ) : (
          <table className="data-table patients-table" id="dentists-table">
            <thead>
              <tr>
                <th>Dentist ID</th>
                <th>Practitioner Name</th>
                <th>Specialization</th>
                <th>Contact Phone</th>
                <th>Email Address</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredDentists.map((dentist) => (
                <tr key={dentist.id}>
                  <td>
                    <span className="patient-number-badge">{dentist.dentistNumber}</span>
                  </td>
                  <td>
                    <div className="cell-fullname font-semibold">{dentist.fullName}</div>
                  </td>
                  <td>
                    <span className="gender-tag gender-male">{dentist.specialization}</span>
                  </td>
                  <td>
                    <div className="contact-phone">{dentist.phone}</div>
                  </td>
                  <td>
                    <div className="contact-email">{dentist.email || '—'}</div>
                  </td>
                  <td>
                    <span className={`status-pill ${dentist.active ? 'status-active' : 'status-inactive'}`}>
                      <span className="dot"></span>
                      {dentist.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="table-actions">
                      <button
                        className="action-btn action-view"
                        onClick={() => setViewingDentist(dentist)}
                        title="View Details"
                      >
                        <Eye size={14} style={{ display: 'inline', marginRight: '3px' }} />
                        View
                      </button>
                      <button
                        className="action-btn action-edit"
                        onClick={() => openEditModal(dentist)}
                        title="Edit Practitioner"
                      >
                        <Edit3 size={14} style={{ display: 'inline', marginRight: '3px' }} />
                        Edit
                      </button>
                      <button
                        className="action-btn action-delete"
                        onClick={() => setDeletingDentist(dentist)}
                        title="Delete Practitioner"
                      >
                        <Trash2 size={14} style={{ display: 'inline', marginRight: '3px' }} />
                        Delete
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
                {editingDentist ? `Edit Practitioner #${editingDentist.dentistNumber}` : 'Register New Dentist'}
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
                <label className="form-label">Dentist ID *</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g. DEN-101"
                    value={formData.dentistNumber}
                    onChange={(e) => setFormData({ ...formData, dentistNumber: e.target.value })}
                    disabled={submitting}
                    autoFocus
                  />
                  {!editingDentist && (
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={generateDentistNumber}
                      title="Auto Generate ID"
                      style={{ padding: '0 0.85rem', whiteSpace: 'nowrap', fontSize: '0.8rem' }}
                    >
                      <Sparkles size={14} style={{ display: 'inline', marginRight: '4px' }} />
                      Auto
                    </button>
                  )}
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Practitioner Full Name *</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Dr. Emily Thorne"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  disabled={submitting}
                />
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Specialization *</label>
                  <select
                    className="form-input"
                    value={formData.specialization}
                    onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
                    disabled={submitting}
                  >
                    {SPECIALIZATIONS.map((spec) => (
                      <option key={spec} value={spec}>
                        {spec}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Phone Number *</label>
                  <input
                    type="tel"
                    className="form-input"
                    placeholder="e.g. +1-555-0199"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Email Address (Optional)</label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="e.g. emily.thorne@dentalcare.com"
                  value={formData.email || ''}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  disabled={submitting}
                />
              </div>

              <div className="form-group" style={{ marginTop: '0.25rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', cursor: 'pointer', fontSize: '0.9rem', fontWeight: 600 }}>
                  <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    disabled={submitting}
                    style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                  />
                  <span>Active Practicing Status (Available for patient appointment bookings)</span>
                </label>
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
                  id="btn-save-dentist"
                >
                  {submitting ? 'Saving...' : editingDentist ? 'Update Dentist' : 'Register Dentist'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* VIEW DETAILS MODAL */}
      {viewingDentist && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card">
            <div className="modal-header">
              <h2 className="auth-title">Practitioner Profile</h2>
              <button className="modal-close" onClick={() => setViewingDentist(null)}>&times;</button>
            </div>

            <div className="profile-details-grid" style={{ padding: '1rem 0' }}>
              <div className="detail-item">
                <span className="detail-label">Dentist ID</span>
                <span className="detail-value">#{viewingDentist.dentistNumber}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Full Name</span>
                <span className="detail-value">{viewingDentist.fullName}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Specialization</span>
                <span className="detail-value" style={{ color: '#0284c7' }}>{viewingDentist.specialization}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Practicing Status</span>
                <span className="detail-value">
                  <span className={`status-pill ${viewingDentist.active ? 'status-active' : 'status-inactive'}`} style={{ display: 'inline-flex' }}>
                    <span className="dot"></span>
                    {viewingDentist.active ? 'Active' : 'Inactive'}
                  </span>
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Phone</span>
                <span className="detail-value">{viewingDentist.phone}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Email</span>
                <span className="detail-value">{viewingDentist.email || 'Not provided'}</span>
              </div>
            </div>

            <div className="modal-actions">
              <button
                className="btn-secondary"
                onClick={() => setViewingDentist(null)}
              >
                Close
              </button>
              <button
                className="btn-primary"
                onClick={() => {
                  const d = viewingDentist;
                  setViewingDentist(null);
                  openEditModal(d);
                }}
              >
                <Edit3 size={14} style={{ display: 'inline', marginRight: '4px' }} />
                Edit Profile
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deletingDentist && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card" style={{ borderTop: '4px solid #ef4444' }}>
            <div className="modal-header">
              <h2 className="auth-title" style={{ color: '#ef4444' }}>Delete Practitioner</h2>
              <button className="modal-close" onClick={() => setDeletingDentist(null)}>&times;</button>
            </div>

            <div style={{ margin: '1rem 0', color: '#334155', lineHeight: 1.5 }}>
              <p style={{ marginBottom: '0.75rem' }}>
                Are you sure you want to delete practitioner record for <strong>{deletingDentist.fullName}</strong> ({deletingDentist.dentistNumber})?
              </p>
              <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
                Note: In accordance with relational database integrity constraints, dentists with existing scheduled appointments cannot be deleted.
              </p>
            </div>

            <div className="modal-actions">
              <button
                className="btn-secondary"
                onClick={() => setDeletingDentist(null)}
                disabled={submitting}
              >
                Cancel
              </button>
              <button
                className="btn-primary"
                style={{ backgroundColor: '#ef4444', borderColor: '#ef4444' }}
                onClick={handleDeleteConfirm}
                disabled={submitting}
                id="btn-confirm-delete-dentist"
              >
                {submitting ? 'Deleting...' : 'Confirm Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
