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
    setFormError(null);
    setSelectedAppointmentId(0);
    setConsultationFee(0);
    setTreatmentAmount(0);
    setBillDate(new Date().toISOString().split('T')[0]);
    setBillStatus('PENDING');
    setShowCreateModal(true);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAppointmentId) {
      setFormError('Please select an appointment to bill');
      return;
    }

    setSubmitting(true);
    setFormError(null);

    const payload: BillRequest = {
      appointmentId: selectedAppointmentId,
      consultationFee: Number(consultationFee),
      treatmentAmount: Number(treatmentAmount),
      billDate: billDate,
      status: billStatus,
    };

    try {
      const created = await billingService.createBill(payload);
      setNotice(`Bill #${created.billNumber} created successfully!`);
      setShowCreateModal(false);
      fetchBills();
      setTimeout(() => setNotice(null), 5000);
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
      setNotice(`Bill #${bill.billNumber} updated to ${newStatus}`);
      fetchBills();
      setUpdatingBill(null);
      setTimeout(() => setNotice(null), 4000);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to update bill status');
      setTimeout(() => setError(null), 5000);
    }
  };

  const openReceiptModal = async (billId: number) => {
    try {
      const receipt = await billingService.getReceipt(billId);
      setViewingReceipt(receipt);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to retrieve receipt');
      setTimeout(() => setError(null), 4000);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingBill) return;

    try {
      await billingService.deleteBill(deletingBill.id);
      setNotice(`Bill #${deletingBill.billNumber} deleted successfully.`);
      setDeletingBill(null);
      fetchBills();
      setTimeout(() => setNotice(null), 4000);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete bill');
      setDeletingBill(null);
      setTimeout(() => setError(null), 5000);
    }
  };

  // Derived statistics for summary pill cards
  const totalBilled = bills.reduce((acc, b) => acc + (Number(b.totalAmount) || 0), 0);
  const totalCollected = bills
    .filter((b) => b.status === 'PAID')
    .reduce((acc, b) => acc + (Number(b.totalAmount) || 0), 0);
  const pendingAmount = bills
    .filter((b) => b.status === 'PENDING')
    .reduce((acc, b) => acc + (Number(b.totalAmount) || 0), 0);

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
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Billing & Invoicing</h1>
          <p className="page-subtitle">Manage patient invoices, payment status, receipts, and fee breakdowns.</p>
        </div>
        <div className="header-actions">
          <button className="btn-primary" onClick={openCreateModal}>
            + Create Bill
          </button>
        </div>
      </div>

      {/* Notice & Error banners */}
      {notice && <div className="banner-success">{notice}</div>}
      {error && <div className="banner-error">{error}</div>}

      {/* Summary KPI Pills */}
      <div className="billing-kpi-row">
        <div className="kpi-card kpi-total">
          <span className="kpi-label">Total Invoiced</span>
          <span className="kpi-value">${totalBilled.toFixed(2)}</span>
          <span className="kpi-subtext">{bills.length} Total Bills</span>
        </div>
        <div className="kpi-card kpi-paid">
          <span className="kpi-label">Collected (Paid)</span>
          <span className="kpi-value">${totalCollected.toFixed(2)}</span>
          <span className="kpi-subtext">{bills.filter((b) => b.status === 'PAID').length} Paid Bills</span>
        </div>
        <div className="kpi-card kpi-pending">
          <span className="kpi-label">Outstanding (Pending)</span>
          <span className="kpi-value">${pendingAmount.toFixed(2)}</span>
          <span className="kpi-subtext">{bills.filter((b) => b.status === 'PENDING').length} Unpaid</span>
        </div>
      </div>

      {/* Filters Card */}
      <div className="filter-card">
        <div className="filter-grid">
          <div className="filter-group">
            <label>Search Bill #</label>
            <input
              type="text"
              placeholder="e.g. BIL-..."
              value={filters.billNumber || ''}
              onChange={(e) => setFilters({ ...filters, billNumber: e.target.value })}
            />
          </div>

          <div className="filter-group">
            <label>Payment Status</label>
            <select
              value={filters.status || ''}
              onChange={(e) => setFilters({ ...filters, status: (e.target.value as BillStatus) || undefined })}
            >
              <option value="">All Statuses</option>
              <option value="PENDING">PENDING</option>
              <option value="PAID">PAID</option>
              <option value="CANCELLED">CANCELLED</option>
              <option value="REFUNDED">REFUNDED</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Bill Date</label>
            <input
              type="date"
              value={filters.date || ''}
              onChange={(e) => setFilters({ ...filters, date: e.target.value })}
            />
          </div>

          <div className="filter-group filter-actions-group">
            <label>&nbsp;</label>
            <button
              className="btn-secondary"
              onClick={() => setFilters({ status: undefined, date: '', billNumber: '' })}
            >
              Reset Filters
            </button>
          </div>
        </div>
      </div>

      {/* Bills Table */}
      <div className="table-wrapper">
        {loading ? (
          <div className="table-loading">Loading bills...</div>
        ) : bills.length === 0 ? (
          <div className="empty-state">
            <h3>No billing records found</h3>
            <p>Try clearing filters or generate a new bill for an appointment.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Bill #</th>
                <th>Date</th>
                <th>Patient</th>
                <th>Dentist</th>
                <th>Treatment</th>
                <th style={{ textAlign: 'right' }}>Consult Fee</th>
                <th style={{ textAlign: 'right' }}>Treatment Fee</th>
                <th style={{ textAlign: 'right' }}>Total</th>
                <th>Status</th>
                <th style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bills.map((bill) => (
                <tr key={bill.id}>
                  <td className="font-semibold text-primary">{bill.billNumber}</td>
                  <td>{bill.billDate}</td>
                  <td>
                    <div><strong>{bill.patientName}</strong></div>
                    <div className="text-muted text-xs">{bill.patientNumber}</div>
                  </td>
                  <td>{bill.dentistName}</td>
                  <td>
                    <div>{bill.treatmentName}</div>
                    <div className="text-muted text-xs">{bill.treatmentCode}</div>
                  </td>
                  <td style={{ textAlign: 'right' }}>${Number(bill.consultationFee).toFixed(2)}</td>
                  <td style={{ textAlign: 'right' }}>${Number(bill.treatmentAmount).toFixed(2)}</td>
                  <td style={{ textAlign: 'right', fontWeight: 600 }}>${Number(bill.totalAmount).toFixed(2)}</td>
                  <td>
                    <span className={`status-badge ${getStatusBadgeClass(bill.status)}`}>
                      {bill.status}
                    </span>
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <div className="table-actions-cell">
                      <button
                        className="btn-action-receipt"
                        title="View Official Receipt"
                        onClick={() => openReceiptModal(bill.id)}
                      >
                        Receipt
                      </button>

                      {bill.status === 'PENDING' && (
                        <button
                          className="btn-action-pay"
                          title="Mark as Paid"
                          onClick={() => handleQuickStatusUpdate(bill, 'PAID')}
                        >
                          Mark Paid
                        </button>
                      )}

                      <button
                        className="btn-action-edit"
                        title="Update Status"
                        onClick={() => setUpdatingBill(bill)}
                      >
                        Status
                      </button>

                      <button
                        className="btn-action-delete"
                        title={bill.status === 'PAID' ? 'Paid bills cannot be deleted' : 'Delete bill'}
                        disabled={bill.status === 'PAID'}
                        onClick={() => setDeletingBill(bill)}
                      >
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

      {/* CREATE BILL MODAL */}
      {showCreateModal && (
        <div className="modal-backdrop" onClick={() => setShowCreateModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Issue Appointment Invoice</h2>
              <button className="modal-close" onClick={() => setShowCreateModal(false)}>
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateSubmit}>
              <div className="modal-body">
                {formError && <div className="banner-error mb-4">{formError}</div>}

                <div className="form-group">
                  <label>Select Appointment *</label>
                  <select
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
                    <small className="text-muted">No unbilled appointments found. Existing appointments already have bills.</small>
                  )}
                </div>

                {selectedAppointmentId > 0 && (
                  <div className="selected-appt-preview">
                    {(() => {
                      const appt = appointments.find((a) => a.id === selectedAppointmentId);
                      if (!appt) return null;
                      return (
                        <div className="preview-card">
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
                    <label>Consultation Fee ($)</label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={consultationFee}
                      onChange={(e) => setConsultationFee(Number(e.target.value))}
                    />
                  </div>

                  <div className="form-group">
                    <label>Treatment Amount ($)</label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={treatmentAmount}
                      onChange={(e) => setTreatmentAmount(Number(e.target.value))}
                    />
                  </div>
                </div>

                <div className="form-row-2">
                  <div className="form-group">
                    <label>Bill Date</label>
                    <input
                      type="date"
                      value={billDate}
                      onChange={(e) => setBillDate(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Initial Status</label>
                    <select
                      value={billStatus}
                      onChange={(e) => setBillStatus(e.target.value as BillStatus)}
                    >
                      <option value="PENDING">PENDING</option>
                      <option value="PAID">PAID</option>
                    </select>
                  </div>
                </div>

                {/* Live Total Calculation */}
                <div className="live-total-box">
                  <span>Grand Total:</span>
                  <span className="live-total-value">
                    ${(Number(consultationFee || 0) + Number(treatmentAmount || 0)).toFixed(2)}
                  </span>
                </div>
              </div>

              <div className="modal-footer">
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
        <div className="modal-backdrop" onClick={() => setUpdatingBill(null)}>
          <div className="modal-content modal-sm" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Update Payment Status</h2>
              <button className="modal-close" onClick={() => setUpdatingBill(null)}>
                &times;
              </button>
            </div>
            <div className="modal-body">
              <p>
                Update payment status for bill <strong>{updatingBill.billNumber}</strong> ({updatingBill.patientName}):
              </p>
              <div className="status-button-grid mt-4">
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
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setUpdatingBill(null)}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* RECEIPT MODAL */}
      {viewingReceipt && (
        <div className="modal-backdrop" onClick={() => setViewingReceipt(null)}>
          <div className="modal-content receipt-modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header no-print">
              <h2>Official Dental Receipt</h2>
              <div className="modal-header-actions">
                <button className="btn-primary btn-sm" onClick={() => window.print()}>
                  Print / Save Receipt
                </button>
                <button className="modal-close" onClick={() => setViewingReceipt(null)}>
                  &times;
                </button>
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

            <div className="modal-footer no-print">
              <button className="btn-secondary" onClick={() => setViewingReceipt(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deletingBill && (
        <div className="modal-backdrop" onClick={() => setDeletingBill(null)}>
          <div className="modal-content modal-sm" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Confirm Deletion</h2>
              <button className="modal-close" onClick={() => setDeletingBill(null)}>
                &times;
              </button>
            </div>
            <div className="modal-body">
              <p>
                Are you sure you want to delete bill <strong>{deletingBill.billNumber}</strong> for patient{' '}
                <strong>{deletingBill.patientName}</strong>?
              </p>
              <div className="banner-warning mt-2">
                Note: Only unpaid/cancelled bills can be deleted. Completed payments are protected for audit compliance.
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setDeletingBill(null)}>
                Cancel
              </button>
              <button className="btn-danger" onClick={handleDeleteConfirm}>
                Confirm Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
