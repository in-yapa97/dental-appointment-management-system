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
  Phone,
  Mail,
  Award,
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
    <div className="page-container">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <Stethoscope className="title-icon text-primary" size={28} />
            Dentists & Practitioners
          </h1>
          <p className="page-description">
            Manage licensed dental practitioners, clinical specializations, contact details, and practicing status.
          </p>
        </div>
        <button className="btn-primary" onClick={openCreateModal} id="btn-add-dentist">
          <UserPlus size={18} />
          <span>Add Dentist</span>
        </button>
      </div>

      {/* Notices and Alerts */}
      {notice && (
        <div className="alert-box alert-success">
          <CheckCircle2 size={18} />
          <span>{notice}</span>
          <button className="close-alert-btn" onClick={() => setNotice(null)}>&times;</button>
        </div>
      )}

      {error && (
        <div className="alert-box alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
          <button className="close-alert-btn" onClick={() => setError(null)}>&times;</button>
        </div>
      )}

      {/* Search and Filters Bar */}
      <div className="table-controls-card">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="search-input"
            placeholder="Search by name, dentist ID (DEN-...), specialization, phone, email..."
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

        <div className="filter-group">
          <select
            className="filter-select"
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
      </div>

      {/* Dentists Data Table */}
      <div className="table-container">
        {loading ? (
          <div className="table-loading-state">
            <div className="spinner"></div>
            <p>Loading practitioner directory...</p>
          </div>
        ) : filteredDentists.length === 0 ? (
          <div className="empty-state">
            <Stethoscope size={48} className="empty-state-icon" />
            <h3>No Dentists Found</h3>
            <p>
              {searchKeyword || selectedSpecialization
                ? 'No practitioners match your search criteria. Try clearing filters.'
                : 'No dental practitioners registered yet. Click "Add Dentist" to register one.'}
            </p>
            {(searchKeyword || selectedSpecialization) && (
              <button
                className="btn-secondary"
                onClick={() => {
                  handleClearSearch();
                  setSelectedSpecialization('');
                }}
              >
                Clear Filters
              </button>
            )}
          </div>
        ) : (
          <table className="data-table" id="dentists-table">
            <thead>
              <tr>
                <th>Dentist ID</th>
                <th>Practitioner Name</th>
                <th>Specialization</th>
                <th>Phone Number</th>
                <th>Email Address</th>
                <th>Status</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredDentists.map((dentist) => (
                <tr key={dentist.id} className="table-row-hover">
                  <td>
                    <span className="badge badge-code">{dentist.dentistNumber}</span>
                  </td>
                  <td>
                    <div className="font-semibold text-main">{dentist.fullName}</div>
                  </td>
                  <td>
                    <span className="badge badge-info">
                      <Award size={12} className="inline mr-1" />
                      {dentist.specialization}
                    </span>
                  </td>
                  <td>
                    <div className="flex items-center text-muted">
                      <Phone size={13} className="mr-1 text-muted" />
                      {dentist.phone}
                    </div>
                  </td>
                  <td>
                    {dentist.email ? (
                      <div className="flex items-center text-muted">
                        <Mail size={13} className="mr-1 text-muted" />
                        {dentist.email}
                      </div>
                    ) : (
                      <span className="text-muted italic">None</span>
                    )}
                  </td>
                  <td>
                    {dentist.active ? (
                      <span className="badge badge-success">Active</span>
                    ) : (
                      <span className="badge badge-secondary">Inactive</span>
                    )}
                  </td>
                  <td className="text-right">
                    <div className="action-buttons-group">
                      <button
                        className="action-btn action-view"
                        onClick={() => setViewingDentist(dentist)}
                        title="View Details"
                      >
                        <Eye size={15} />
                      </button>
                      <button
                        className="action-btn action-edit"
                        onClick={() => openEditModal(dentist)}
                        title="Edit Practitioner"
                      >
                        <Edit3 size={15} />
                      </button>
                      <button
                        className="action-btn action-delete"
                        onClick={() => setDeletingDentist(dentist)}
                        title="Delete Practitioner"
                      >
                        <Trash2 size={15} />
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
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3 className="modal-title">
                {editingDentist ? (
                  <>
                    <Edit3 size={20} className="text-primary mr-2" />
                    Edit Practitioner Details
                  </>
                ) : (
                  <>
                    <UserPlus size={20} className="text-primary mr-2" />
                    Add New Dentist
                  </>
                )}
              </h3>
              <button className="modal-close-btn" onClick={() => setShowFormModal(false)}>&times;</button>
            </div>

            <form onSubmit={handleFormSubmit}>
              <div className="modal-body">
                {formError && (
                  <div className="alert-box alert-danger mb-4">
                    <AlertCircle size={16} />
                    <span>{formError}</span>
                  </div>
                )}

                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">
                      Dentist ID <span className="text-danger">*</span>
                    </label>
                    <div className="flex gap-2">
                      <input
                        type="text"
                        className="form-input"
                        placeholder="e.g. DEN-101"
                        value={formData.dentistNumber}
                        onChange={(e) => setFormData({ ...formData, dentistNumber: e.target.value })}
                        required
                        id="input-dentist-number"
                      />
                      {!editingDentist && (
                        <button
                          type="button"
                          className="btn-secondary whitespace-nowrap text-xs"
                          onClick={generateDentistNumber}
                          title="Generate Unique Dentist ID"
                        >
                          <Sparkles size={14} className="mr-1" />
                          Auto
                        </button>
                      )}
                    </div>
                    <span className="form-helper">Unique practitioner registration ID</span>
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      Full Name <span className="text-danger">*</span>
                    </label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g. Dr. Emily Thorne"
                      value={formData.fullName}
                      onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                      required
                      id="input-dentist-name"
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      Specialization <span className="text-danger">*</span>
                    </label>
                    <select
                      className="form-select"
                      value={formData.specialization}
                      onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
                      required
                      id="select-dentist-specialization"
                    >
                      {SPECIALIZATIONS.map((spec) => (
                        <option key={spec} value={spec}>
                          {spec}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      Phone Number <span className="text-danger">*</span>
                    </label>
                    <input
                      type="tel"
                      className="form-input"
                      placeholder="e.g. +1-555-0199"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      required
                      id="input-dentist-phone"
                    />
                  </div>

                  <div className="form-group full-width">
                    <label className="form-label">Email Address</label>
                    <input
                      type="email"
                      className="form-input"
                      placeholder="e.g. emily.thorne@dentalcare.com"
                      value={formData.email || ''}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      id="input-dentist-email"
                    />
                  </div>

                  <div className="form-group full-width">
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        checked={formData.active}
                        onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                        id="checkbox-dentist-active"
                      />
                      <span>Active Practicing Status (available for appointment bookings)</span>
                    </label>
                  </div>
                </div>
              </div>

              <div className="modal-footer">
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
                  {submitting ? 'Saving...' : editingDentist ? 'Save Changes' : 'Register Dentist'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* VIEW DETAILS MODAL */}
      {viewingDentist && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3 className="modal-title">
                <Stethoscope size={20} className="text-primary mr-2" />
                Practitioner Profile
              </h3>
              <button className="modal-close-btn" onClick={() => setViewingDentist(null)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="profile-card">
                <div className="profile-header">
                  <div className="profile-avatar bg-primary-light text-primary font-bold text-xl">
                    {viewingDentist.fullName.replace('Dr. ', '').charAt(0)}
                  </div>
                  <div>
                    <h4 className="text-lg font-bold text-main">{viewingDentist.fullName}</h4>
                    <span className="badge badge-code">{viewingDentist.dentistNumber}</span>
                  </div>
                </div>

                <div className="profile-grid mt-4">
                  <div className="profile-item">
                    <span className="profile-label">Specialization</span>
                    <span className="profile-value font-medium text-primary">
                      {viewingDentist.specialization}
                    </span>
                  </div>

                  <div className="profile-item">
                    <span className="profile-label">Practicing Status</span>
                    <span className="profile-value">
                      {viewingDentist.active ? (
                        <span className="badge badge-success">Active Practitioner</span>
                      ) : (
                        <span className="badge badge-secondary">Inactive / On Leave</span>
                      )}
                    </span>
                  </div>

                  <div className="profile-item">
                    <span className="profile-label">Phone</span>
                    <span className="profile-value">{viewingDentist.phone}</span>
                  </div>

                  <div className="profile-item">
                    <span className="profile-label">Email Address</span>
                    <span className="profile-value">{viewingDentist.email || 'Not provided'}</span>
                  </div>
                </div>
              </div>
            </div>
            <div className="modal-footer">
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
                <Edit3 size={15} className="mr-1" />
                Edit Profile
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deletingDentist && (
        <div className="modal-backdrop">
          <div className="modal-card modal-danger">
            <div className="modal-header">
              <h3 className="modal-title text-danger">
                <AlertCircle size={20} className="mr-2" />
                Delete Practitioner Record
              </h3>
              <button className="modal-close-btn" onClick={() => setDeletingDentist(null)}>&times;</button>
            </div>
            <div className="modal-body">
              <p className="text-main mb-2">
                Are you sure you want to delete practitioner record for{' '}
                <strong>{deletingDentist.fullName}</strong> ({deletingDentist.dentistNumber})?
              </p>
              <p className="text-muted text-sm">
                Note: In accordance with relational database integrity constraints, dentists with existing scheduled appointments cannot be deleted.
              </p>
            </div>
            <div className="modal-footer">
              <button
                className="btn-secondary"
                onClick={() => setDeletingDentist(null)}
                disabled={submitting}
              >
                Cancel
              </button>
              <button
                className="btn-danger"
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
