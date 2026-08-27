import React, { useState, useEffect, useCallback } from 'react';
import {
  Appointment,
  Bill,
  BillRequest,
  BillStatus,
  ReceiptResponse,
} from '../types';
import { billingService, BillFilters } from '../services/billingService';
import { appointmentService } from '../services/appointmentService';
import {
  Receipt,
  CheckCircle2,
  AlertCircle,
  DollarSign,
  Clock,
  CreditCard,
  Search,
  Printer,
  RotateCcw,
  Edit3,
  Trash2,
} from 'lucide-react';

export const BillingPage: React.FC = () => {
  const [bills, setBills] = useState<Bill[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  // Filters
  const [filters, setFilters] = useState<BillFilters>({
    status: undefined,
    date: '',
    billNumber: '',
  });

  // Modals state
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false);
  const [viewingReceipt, setViewingReceipt] = useState<ReceiptResponse | null>(null);
  const [deletingBill, setDeletingBill] = useState<Bill | null>(null);
  const [updatingBill, setUpdatingBill] = useState<Bill | null>(null);

  // Create Bill Form State
  const [selectedAppointmentId, setSelectedAppointmentId] = useState<number>(0);
  const [consultationFee, setConsultationFee] = useState<number>(0);
  const [treatmentAmount, setTreatmentAmount] = useState<number>(0);
  const [billDate, setBillDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [billStatus, setBillStatus] = useState<BillStatus>('PENDING');
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Fetch Bills
  const fetchBills = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await billingService.getBills(filters);
      setBills(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load bills');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  // Fetch Appointments for Bill creation
  const fetchAppointments = useCallback(async () => {
    try {
      const data = await appointmentService.getAppointments();
      setAppointments(data);
    } catch {
      // Non-blocking
    }
  }, []);

  useEffect(() => {
    fetchBills();
  }, [fetchBills]);

  useEffect(() => {
    fetchAppointments();
  }, [fetchAppointments]);

  // When selected appointment changes, auto-populate treatment fee
  const handleAppointmentChange = (appId: number) => {
    setSelectedAppointmentId(appId);
    const selected = appointments.find((a) => a.id === appId);
    if (selected) {
      const defaultCost = Number(selected.treatmentCost) || 0;
      setTreatmentAmount(defaultCost);
    } else {
      setTreatmentAmount(0);
    }
  };

  const openCreateModal = () => {
    setSelectedAppointmentId(0);
    setConsultationFee(0);
    setTreatmentAmount(0);
    setBillDate(new Date().toISOString().split('T')[0]);
    setBillStatus('PENDING');
    setFormError(null);
    setShowCreateModal(true);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!selectedAppointmentId || selectedAppointmentId === 0) {
      setFormError('Please select an appointment to bill');
      return;
    }
    if (consultationFee < 0 || treatmentAmount < 0) {
      setFormError('Fees cannot be negative');
      return;
    }

    setSubmitting(true);
    try {
      const payload: BillRequest = {
        appointmentId: selectedAppointmentId,
        consultationFee,
        treatmentAmount,
        billDate,
        status: billStatus,
      };

      const created = await billingService.createBill(payload);
      setNotice(`Bill #${created.billNumber} created successfully! Total: $${Number(created.totalAmount).toFixed(2)}`);
      setShowCreateModal(false);
      fetchBills();
    } catch (err: unknown) {
      setFormError(err instanceof Error ? err.message : 'Failed to create bill');
    } finally {
      setSubmitting(false);
    }
  };

  const handleQuickStatusUpdate = async (bill: Bill, newStatus: BillStatus) => {
    try {
      await billingService.updateBill(bill.id, {
        appointmentId: bill.appointmentId,
        consultationFee: bill.consultationFee,
        treatmentAmount: bill.treatmentAmount,
        billDate: bill.billDate,
        status: newStatus,
      });
      setNotice(`Bill #${bill.billNumber} status updated to ${newStatus}.`);
      fetchBills();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to update status');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingBill) return;
    setSubmitting(true);
    setError(null);
    try {
      await billingService.deleteBill(deletingBill.id);
      setNotice(`Bill #${deletingBill.billNumber} deleted successfully.`);
      setDeletingBill(null);
      fetchBills();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete bill');
      setDeletingBill(null);
    } finally {
      setSubmitting(false);
    }
  };

  const openReceiptModal = async (billId: number) => {
    setError(null);
    try {
      const receipt = await billingService.getReceipt(billId);
      setViewingReceipt(receipt);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to fetch receipt');
    }
  };

  // KPI Calculations
  const totalBilled = bills.reduce((sum, b) => sum + (Number(b.totalAmount) || 0), 0);
  const totalCollected = bills
    .filter((b) => b.status === 'PAID')
    .reduce((sum, b) => sum + (Number(b.totalAmount) || 0), 0);
  const pendingAmount = bills
    .filter((b) => b.status === 'PENDING')
    .reduce((sum, b) => sum + (Number(b.totalAmount) || 0), 0);

  const getStatusBadgeClass = (status: BillStatus): string => {
    switch (status) {
      case 'PAID':
        return 'badge-status-paid';
      case 'PENDING':
        return 'badge-status-pending';
      case 'CANCELLED':
        return 'badge-status-cancelled';
      case 'REFUNDED':
        return 'badge-status-refunded';
      default:
        return '';
    }
  };

  // Find appointments that don't have bills yet
  const billedAppointmentIds = new Set(bills.map((b) => b.appointmentId));
  const availableAppointments = appointments.filter((a) => !billedAppointmentIds.has(a.id));

  return (
    <div className="billing-page-container">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Invoicing &amp; Billing</h1>
          <p className="page-subtitle">Manage patient invoices, payment status, receipts, and fee breakdowns</p>
        </div>
        <div className="header-actions">
          <button className="btn-primary" onClick={openCreateModal}>
            <Receipt size={16} />
            <span>Issue Invoice</span>
          </button>
        </div>
      </div>

      {/* Notice & Error banners */}
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

      {/* Summary KPI Pills */}
      <div className="dashboard-kpi-grid" style={{ marginBottom: '1.5rem' }}>
        <div className="kpi-card">
          <div className="kpi-top">
            <span className="kpi-label">Total Invoiced</span>
            <div className="kpi-icon-box icon-sky">
              <CreditCard size={20} />
            </div>
          </div>
          <span className="kpi-value">${totalBilled.toFixed(2)}</span>
          <div className="kpi-footer">
            <span className="kpi-subtext">{bills.length} total bills</span>
          </div>
        </div>
        <div className="kpi-card">
          <div className="kpi-top">
            <span className="kpi-label">Collected (Paid)</span>
            <div className="kpi-icon-box icon-emerald">
              <DollarSign size={20} />
            </div>
          </div>
          <span className="kpi-value">${totalCollected.toFixed(2)}</span>
          <div className="kpi-footer">
            <span className="kpi-subtext">{bills.filter((b) => b.status === 'PAID').length} paid bills</span>
          </div>
        </div>
        <div className="kpi-card">
          <div className="kpi-top">
            <span className="kpi-label">Outstanding (Pending)</span>
            <div className="kpi-icon-box icon-amber">
              <Clock size={20} />
            </div>
          </div>
          <span className="kpi-value">${pendingAmount.toFixed(2)}</span>
          <div className="kpi-footer">
            <span className="kpi-subtext">{bills.filter((b) => b.status === 'PENDING').length} awaiting payment</span>
          </div>
        </div>
      </div>

      {/* Filters Card */}
      <div className="filter-bar-card">
        <div className="filter-item">
          <label className="filter-label">Search Bill #</label>
          <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
            <Search size={16} style={{ position: 'absolute', left: '0.85rem', color: '#94a3b8', pointerEvents: 'none' }} />
            <input
              type="text"
              className="form-input filter-select"
              style={{ paddingLeft: '2.5rem' }}
              placeholder="e.g. BIL-..."
              value={filters.billNumber || ''}
              onChange={(e) => setFilters({ ...filters, billNumber: e.target.value })}
              id="filter-bill-number"
            />
          </div>
        </div>

        <div className="filter-item">
          <label className="filter-label">Payment Status</label>
          <select
            className="form-input filter-select"
            value={filters.status || ''}
            onChange={(e) => setFilters({ ...filters, status: (e.target.value as BillStatus) || undefined })}
            id="filter-bill-status"
          >
            <option value="">All Statuses</option>
            <option value="PENDING">PENDING</option>
            <option value="PAID">PAID</option>
            <option value="CANCELLED">CANCELLED</option>
            <option value="REFUNDED">REFUNDED</option>
          </select>
        </div>

        <div className="filter-item">
          <label className="filter-label">Bill Date</label>
          <input
            type="date"
            className="form-input filter-select"
            value={filters.date || ''}
            onChange={(e) => setFilters({ ...filters, date: e.target.value })}
            id="filter-bill-date"
          />
        </div>

        <div className="filter-actions">
          <button
            className="btn-secondary btn-sm"
            onClick={() => setFilters({ status: undefined, date: '', billNumber: '' })}
            id="btn-reset-bill-filters"
          >
            <RotateCcw size={14} />
            <span>Reset</span>
          </button>
        </div>
      </div>

      {/* Bills Table */}
      <div className="table-wrapper">
        {loading ? (
          <div className="table-loading">
            <div className="spinner"></div>
            <span>Loading billing records...</span>
          </div>
        ) : bills.length === 0 ? (
          <div className="empty-state">
            <Receipt size={42} style={{ color: '#94a3b8', marginBottom: '0.75rem' }} />
            <h3>No Billing Records Found</h3>
            <p>Try clearing filters or click "+ Issue Invoice" to generate a bill for an appointment.</p>
          </div>
        ) : (
          <table className="data-table patients-table">
            <thead>
              <tr>
                <th style={{ whiteSpace: 'nowrap' }}>Bill #</th>
                <th style={{ whiteSpace: 'nowrap' }}>Date</th>
                <th>Patient</th>
                <th>Dentist</th>
                <th>Treatment</th>
                <th style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>Consult</th>
                <th style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>Treatment Fee</th>
                <th style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>Total</th>
                <th style={{ whiteSpace: 'nowrap' }}>Status</th>
                <th style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bills.map((bill) => (
                <tr key={bill.id} id={`bill-row-${bill.id}`}>
                  <td>
                    <span className="patient-number-badge" style={{ whiteSpace: 'nowrap' }}>{bill.billNumber}</span>
                  </td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{bill.billDate}</div>
                  </td>
                  <td>
                    <div className="patient-name-cell">
                      <span className="cell-fullname">{bill.patientName}</span>
                      <span className="cell-subtext">{bill.patientNumber}</span>
                    </div>
                  </td>
                  <td>
                    <div className="cell-fullname" style={{ whiteSpace: 'nowrap' }}>{bill.dentistName}</div>
                  </td>
                  <td>
                    <div className="patient-name-cell">
                      <span className="cell-fullname">{bill.treatmentName}</span>
                      <span className="cell-subtext">{bill.treatmentCode}</span>
                    </div>
                  </td>
                  <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                    ${Number(bill.consultationFee).toFixed(2)}
                  </td>
                  <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                    ${Number(bill.treatmentAmount).toFixed(2)}
                  </td>
                  <td style={{ textAlign: 'right', whiteSpace: 'nowrap', fontWeight: 700, color: '#0284c7' }}>
                    ${Number(bill.totalAmount).toFixed(2)}
                  </td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <span className={`status-badge ${getStatusBadgeClass(bill.status)}`}>
                      {bill.status}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="table-actions" style={{ justifyContent: 'flex-end', gap: '0.35rem', flexWrap: 'nowrap' }}>
                      <button
                        className="action-btn action-view"
                        title="View Official Receipt"
                        onClick={() => openReceiptModal(bill.id)}
                        id={`btn-receipt-${bill.id}`}
                      >
                        <Printer size={14} />
                        <span>Receipt</span>
                      </button>

                      {bill.status === 'PENDING' && (
                        <button
                          className="action-btn action-edit"
                          title="Mark as Paid"
                          onClick={() => handleQuickStatusUpdate(bill, 'PAID')}
                          id={`btn-pay-${bill.id}`}
                        >
                          <CheckCircle2 size={14} />
                          <span>Pay</span>
                        </button>
                      )}

                      <button
                        className="action-btn action-view"
                        title="Update Status"
                        onClick={() => setUpdatingBill(bill)}
                        id={`btn-status-${bill.id}`}
                      >
                        <Edit3 size={14} />
                      </button>

                      <button
                        className="action-btn action-delete"
                        title={bill.status === 'PAID' ? 'Paid bills cannot be deleted' : 'Delete bill'}
                        disabled={bill.status === 'PAID'}
                        onClick={() => setDeletingBill(bill)}
                        id={`btn-delete-bill-${bill.id}`}
                        style={bill.status === 'PAID' ? { opacity: 0.4, cursor: 'not-allowed' } : {}}
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

      {/* CREATE BILL MODAL */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card" style={{ maxWidth: '600px' }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                <Receipt size={22} style={{ color: '#0284c7' }} />
                <h2 className="auth-title" style={{ fontSize: '1.25rem', margin: 0 }}>Issue Appointment Invoice</h2>
              </div>
              <button className="modal-close" onClick={() => setShowCreateModal(false)}>&times;</button>
            </div>

            {formError && (
              <div className="alert-box alert-error" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={16} />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleCreateSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Select Appointment *</label>
                <select
                  className="form-input"
                  value={selectedAppointmentId}
                  onChange={(e) => handleAppointmentChange(Number(e.target.value))}
                  required
                >
                  <option value="0">-- Choose an unbilled appointment --</option>
                  {availableAppointments.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.appointmentNumber} — {a.patientName} ({a.treatmentName}, {a.appointmentDate})
                    </option>
                  ))}
                </select>
                {availableAppointments.length === 0 && (
                  <small style={{ color: '#64748b', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                    No unbilled appointments found. Existing appointments already have bills.
                  </small>
                )}
              </div>

              {selectedAppointmentId > 0 && (
                <div className="selected-appt-preview" style={{ marginBottom: '1rem' }}>
                  {(() => {
                    const appt = appointments.find((a) => a.id === selectedAppointmentId);
                    if (!appt) return null;
                    return (
                      <div className="preview-card" style={{ backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '0.85rem 1rem', fontSize: '0.875rem' }}>
                        <div><strong>Patient:</strong> {appt.patientName} ({appt.patientNumber})</div>
                        <div><strong>Dentist:</strong> {appt.dentistName}</div>
                        <div><strong>Treatment:</strong> {appt.treatmentName} (${Number(appt.treatmentCost).toFixed(2)})</div>
                        <div><strong>Date/Time:</strong> {appt.appointmentDate} at {appt.appointmentTime}</div>
                      </div>
                    );
                  })()}
                </div>
              )}

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Consultation Fee ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-input"
                    value={consultationFee}
                    onChange={(e) => setConsultationFee(Number(e.target.value))}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Treatment Amount ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-input"
                    value={treatmentAmount}
                    onChange={(e) => setTreatmentAmount(Number(e.target.value))}
                  />
                </div>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label className="form-label">Bill Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={billDate}
                    onChange={(e) => setBillDate(e.target.value)}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Initial Status</label>
                  <select
                    className="form-input"
                    value={billStatus}
                    onChange={(e) => setBillStatus(e.target.value as BillStatus)}
                  >
                    <option value="PENDING">PENDING</option>
                    <option value="PAID">PAID</option>
                  </select>
                </div>
              </div>

              {/* Live Total Calculation */}
              <div className="live-total-box" style={{ backgroundColor: '#f0f9ff', border: '1px solid #bae6fd', borderRadius: '10px', padding: '0.85rem 1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.5rem' }}>
                <span style={{ fontWeight: 600, color: '#0369a1' }}>Grand Total:</span>
                <span className="live-total-value" style={{ fontSize: '1.4rem', fontWeight: 800, color: '#0284c7' }}>
                  ${(Number(consultationFee || 0) + Number(treatmentAmount || 0)).toFixed(2)}
                </span>
              </div>

              <div className="modal-actions" style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowCreateModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={submitting || selectedAppointmentId === 0}
                >
                  {submitting ? 'Creating...' : 'Create Invoice'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* UPDATE STATUS MODAL */}
      {updatingBill && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card" style={{ maxWidth: '460px' }}>
            <div className="modal-header">
              <h2 className="auth-title" style={{ fontSize: '1.2rem', margin: 0 }}>Update Payment Status</h2>
              <button className="modal-close" onClick={() => setUpdatingBill(null)}>&times;</button>
            </div>
            
            <p style={{ fontSize: '0.9rem', color: '#475569', margin: '1rem 0' }}>
              Update payment status for invoice <strong>#{updatingBill.billNumber}</strong> ({updatingBill.patientName}):
            </p>
            
            <div className="status-button-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1.5rem' }}>
              <button
                className="btn-status-option paid"
                onClick={() => handleQuickStatusUpdate(updatingBill, 'PAID')}
              >
                Mark PAID
              </button>
              <button
                className="btn-status-option pending"
                onClick={() => handleQuickStatusUpdate(updatingBill, 'PENDING')}
              >
                Mark PENDING
              </button>
              <button
                className="btn-status-option cancelled"
                onClick={() => handleQuickStatusUpdate(updatingBill, 'CANCELLED')}
              >
                Mark CANCELLED
              </button>
              <button
                className="btn-status-option refunded"
                onClick={() => handleQuickStatusUpdate(updatingBill, 'REFUNDED')}
              >
                Mark REFUNDED
              </button>
            </div>
            
            <div className="modal-actions" style={{ justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={() => setUpdatingBill(null)}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* RECEIPT MODAL */}
      {viewingReceipt && (
        <div className="modal-overlay">
          <div className="modal-content receipt-modal-card modal-card" style={{ maxWidth: '680px' }}>
            <div className="modal-header no-print" style={{ marginBottom: '1rem', borderBottom: '1px solid #e2e8f0', paddingBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Receipt size={20} style={{ color: '#0284c7' }} />
                <h2 className="auth-title" style={{ fontSize: '1.2rem', margin: 0 }}>Official Dental Receipt</h2>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <button className="btn-primary btn-sm" onClick={() => window.print()}>
                  <Printer size={14} />
                  <span>Print Receipt</span>
                </button>
                <button className="modal-close" onClick={() => setViewingReceipt(null)}>&times;</button>
              </div>
            </div>

            <div className="receipt-paper" id="printable-receipt">
              {/* Receipt Clinic Branding */}
              <div className="receipt-header">
                <div className="receipt-brand">
                  <h1>{viewingReceipt.clinicName}</h1>
                  <p>{viewingReceipt.clinicAddress}</p>
                  <p>{viewingReceipt.clinicContact}</p>
                </div>
                <div className="receipt-meta">
                  <div className="receipt-badge-stamp">{viewingReceipt.paymentStatus}</div>
                  <div><strong>Receipt #:</strong> {viewingReceipt.receiptNumber}</div>
                  <div><strong>Bill #:</strong> {viewingReceipt.billNumber}</div>
                  <div><strong>Date:</strong> {viewingReceipt.paymentDate}</div>
                </div>
              </div>

              <div className="receipt-divider" />

              {/* Patient and Doctor Information */}
              <div className="receipt-info-grid">
                <div className="receipt-info-block">
                  <h4>PATIENT DETAILS</h4>
                  <p><strong>Name:</strong> {viewingReceipt.patientName}</p>
                  <p><strong>Patient #:</strong> {viewingReceipt.patientNumber}</p>
                  {viewingReceipt.patientPhone && <p><strong>Phone:</strong> {viewingReceipt.patientPhone}</p>}
                  {viewingReceipt.patientEmail && <p><strong>Email:</strong> {viewingReceipt.patientEmail}</p>}
                </div>

                <div className="receipt-info-block">
                  <h4>APPOINTMENT & DENTIST</h4>
                  <p><strong>Appointment #:</strong> {viewingReceipt.appointmentNumber}</p>
                  <p><strong>Attending Dentist:</strong> {viewingReceipt.dentistName}</p>
                  {viewingReceipt.dentistSpecialization && (
                    <p><strong>Specialization:</strong> {viewingReceipt.dentistSpecialization}</p>
                  )}
                </div>
              </div>

              <div className="receipt-divider" />

              {/* Line Items Table */}
              <table className="receipt-items-table">
                <thead>
                  <tr>
                    <th>Description</th>
                    <th style={{ textAlign: 'right' }}>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>
                      <div>Consultation / Examination Fee</div>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      ${Number(viewingReceipt.consultationFee).toFixed(2)}
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div>{viewingReceipt.treatmentName}</div>
                      <small className="text-muted">Code: {viewingReceipt.treatmentCode}</small>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      ${Number(viewingReceipt.treatmentAmount).toFixed(2)}
                    </td>
                  </tr>
                </tbody>
                <tfoot>
                  <tr className="receipt-total-row">
                    <td>TOTAL AMOUNT PAID</td>
                    <td style={{ textAlign: 'right' }}>
                      ${Number(viewingReceipt.totalAmount).toFixed(2)}
                    </td>
                  </tr>
                </tfoot>
              </table>

              <div className="receipt-footer">
                <p>Thank you for choosing {viewingReceipt.clinicName}!</p>
                <small>Generated on {new Date(viewingReceipt.issuedAt).toLocaleString()}</small>
              </div>
            </div>

            <div className="modal-actions no-print" style={{ justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="btn-secondary" onClick={() => setViewingReceipt(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deletingBill && (
        <div className="modal-overlay">
          <div className="modal-content auth-card modal-card" style={{ maxWidth: '460px' }}>
            <div className="modal-header">
              <h2 className="auth-title" style={{ color: '#ef4444', fontSize: '1.2rem', margin: 0 }}>Confirm Deletion</h2>
              <button className="modal-close" onClick={() => setDeletingBill(null)}>&times;</button>
            </div>
            
            <p style={{ color: 'var(--text-main)', fontSize: '0.95rem', margin: '1rem 0' }}>
              Are you sure you want to delete invoice <strong>#{deletingBill.billNumber}</strong> for patient{' '}
              <strong>{deletingBill.patientName}</strong>?
            </p>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Audit Rule: Only unpaid/cancelled bills can be deleted. Completed payments are protected for compliance.
            </p>

            <div className="modal-actions" style={{ justifyContent: 'flex-end' }}>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setDeletingBill(null)}
                disabled={submitting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn-danger"
                onClick={handleDeleteConfirm}
                disabled={submitting}
                id="btn-confirm-delete-bill"
              >
                {submitting ? 'Deleting...' : 'Delete Invoice'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
