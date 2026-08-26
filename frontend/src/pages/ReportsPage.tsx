import React, { useState, useEffect, useCallback } from 'react';
import {
  PaymentStatusReportResponse,
  RevenueReportResponse,
  TreatmentRevenueResponse,
} from '../types';
import { billingService } from '../services/billingService';

export const ReportsPage: React.FC = () => {
  const [revenueReport, setRevenueReport] = useState<RevenueReportResponse | null>(null);
  const [paymentStatusReport, setPaymentStatusReport] = useState<PaymentStatusReportResponse | null>(null);
  const [treatmentRevenues, setTreatmentRevenues] = useState<TreatmentRevenueResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Date filters
  const [fromDate, setFromDate] = useState<string>('');
  const [toDate, setToDate] = useState<string>('');

  const fetchReports = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [revData, statusData, trtData] = await Promise.all([
        billingService.getRevenueReport(fromDate || undefined, toDate || undefined),
        billingService.getPaymentStatusReport(),
        billingService.getTreatmentRevenueReport(),
      ]);

      setRevenueReport(revData);
      setPaymentStatusReport(statusData);
      setTreatmentRevenues(trtData);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load report data');
    } finally {
      setLoading(false);
    }
  }, [fromDate, toDate]);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const handleResetFilters = () => {
    setFromDate('');
    setToDate('');
  };

  return (
    <div className="reports-page-container">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Clinic Financial & Operational Reports</h1>
          <p className="page-subtitle">Overview of revenue, collections, procedure volume, and billing health.</p>
        </div>
      </div>

      {error && <div className="banner-error">{error}</div>}

      {/* Date Range Selector */}
      <div className="filter-card mb-6">
        <div className="filter-grid">
          <div className="filter-group">
            <label>From Date</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>To Date</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>

          <div className="filter-group filter-actions-group">
            <label>&nbsp;</label>
            <button className="btn-secondary" onClick={handleResetFilters}>
              All Time
            </button>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="table-loading">Compiling clinic reports...</div>
      ) : (
        <>
          {/* Revenue KPI Cards */}
          {revenueReport && (
            <div className="reports-kpi-grid mb-8">
              <div className="kpi-card kpi-emerald">
                <span className="kpi-label">Total Revenue Collected</span>
                <span className="kpi-value">${Number(revenueReport.totalRevenue).toFixed(2)}</span>
                <span className="kpi-subtext">{revenueReport.paidBills} Invoices Paid</span>
              </div>

              <div className="kpi-card kpi-amber">
                <span className="kpi-label">Outstanding Receivables</span>
                <span className="kpi-value">${Number(revenueReport.unpaidAmount).toFixed(2)}</span>
                <span className="kpi-subtext">{revenueReport.pendingBills} Pending Payments</span>
              </div>

              <div className="kpi-card kpi-indigo">
                <span className="kpi-label">Total Invoices Issued</span>
                <span className="kpi-value">{revenueReport.totalBills}</span>
                <span className="kpi-subtext">All billing records</span>
              </div>

              <div className="kpi-card kpi-rose">
                <span className="kpi-label">Cancelled / Refunded</span>
                <span className="kpi-value">
                  {Number(revenueReport.cancelledBills) + Number(revenueReport.refundedBills)}
                </span>
                <span className="kpi-subtext">
                  {revenueReport.cancelledBills} Cancelled, {revenueReport.refundedBills} Refunded
                </span>
              </div>
            </div>
          )}

          {/* Detailed Reports Grid */}
          <div className="reports-details-grid">
            {/* Payment Status Breakdown */}
            <div className="report-card">
              <div className="report-card-header">
                <h3>Payment Status Breakdown</h3>
                <span className="text-muted text-sm">
                  Total Volume: ${paymentStatusReport ? Number(paymentStatusReport.totalAmount).toFixed(2) : '0.00'}
                </span>
              </div>

              <div className="report-card-body">
                {paymentStatusReport?.breakdown.map((item) => {
                  const percent =
                    paymentStatusReport.totalAmount > 0
                      ? ((Number(item.totalAmount) / Number(paymentStatusReport.totalAmount)) * 100).toFixed(1)
                      : '0';

                  return (
                    <div key={item.status} className="status-progress-item">
                      <div className="status-progress-header">
                        <span className="status-tag font-semibold">{item.status}</span>
                        <span className="status-amount">
                          ${Number(item.totalAmount).toFixed(2)}{' '}
                          <small className="text-muted">({item.count} bills - {percent}%)</small>
                        </span>
                      </div>
                      <div className="progress-bar-bg">
                        <div
                          className={`progress-bar-fill progress-${item.status.toLowerCase()}`}
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Treatment Revenue Analysis */}
            <div className="report-card">
              <div className="report-card-header">
                <h3>Revenue by Treatment Procedure</h3>
                <span className="text-muted text-sm">From Completed / Paid Bills</span>
              </div>

              <div className="report-card-body">
                {treatmentRevenues.length === 0 ? (
                  <p className="text-muted text-sm py-4">No completed treatment revenues recorded yet.</p>
                ) : (
                  <table className="data-table report-table">
                    <thead>
                      <tr>
                        <th>Procedure</th>
                        <th>Code</th>
                        <th style={{ textAlign: 'center' }}>Completed</th>
                        <th style={{ textAlign: 'right' }}>Total Revenue</th>
                      </tr>
                    </thead>
                    <tbody>
                      {treatmentRevenues.map((item) => (
                        <tr key={item.treatmentId}>
                          <td className="font-semibold">{item.treatmentName}</td>
                          <td className="text-muted text-xs">{item.treatmentCode}</td>
                          <td style={{ textAlign: 'center' }}>{item.billCount}</td>
                          <td style={{ textAlign: 'right', fontWeight: 600 }}>
                            ${Number(item.totalRevenue).toFixed(2)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
