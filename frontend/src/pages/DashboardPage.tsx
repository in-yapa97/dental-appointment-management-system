import React, { useState, useEffect, useCallback } from 'react';
import { Appointment, Bill, Patient, User } from '../types';
import { patientService } from '../services/patientService';
import { appointmentService } from '../services/appointmentService';
import { billingService } from '../services/billingService';

interface DashboardPageProps {
  currentUser: User;
  onNavigate: (view: 'dashboard' | 'patients' | 'appointments' | 'billing' | 'reports') => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ currentUser, onNavigate }) => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboardData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [patientsData, apptsData, billsData] = await Promise.all([
        patientService.getPatients().catch(() => []),
        appointmentService.getAppointments().catch(() => []),
        billingService.getBills().catch(() => []),
      ]);

      setPatients(patientsData);
      setAppointments(apptsData);
      setBills(billsData);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load clinic overview');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  // Derived metrics
  const totalPatients = patients.length;
  const totalAppointments = appointments.length;
  const scheduledAppts = appointments.filter((a) => a.status === 'SCHEDULED' || a.status === 'CONFIRMED').length;
  const totalRevenue = bills
    .filter((b) => b.status === 'PAID')
    .reduce((sum, b) => sum + (Number(b.totalAmount) || 0), 0);
  const pendingBills = bills.filter((b) => b.status === 'PENDING').length;

  // Recent lists
  const recentAppointments = appointments.slice(0, 5);
  const recentBills = bills.slice(0, 5);

  const todayFormatted = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="dashboard-page-container">
      {/* Welcome Banner */}
      <div className="dashboard-hero">
        <div className="hero-text">
          <h1 className="hero-title">Welcome back, {currentUser.fullName}</h1>
          <p className="hero-subtitle">
            DentalCare Management Dashboard &bull; {todayFormatted}
          </p>
        </div>
        <div className="hero-actions">
          <button className="btn-primary" onClick={() => onNavigate('appointments')}>
            + Book Appointment
          </button>
          <button className="btn-secondary" onClick={() => onNavigate('patients')}>
            + Add Patient
          </button>
        </div>
      </div>

      {error && <div className="banner-error mb-4">{error}</div>}

      {loading ? (
        <div className="table-loading">Loading clinic metrics...</div>
      ) : (
        <>
          {/* Quick Metrics KPI Cards */}
          <div className="dashboard-kpi-grid">
            <div className="kpi-card kpi-indigo cursor-pointer" onClick={() => onNavigate('patients')}>
              <span className="kpi-label">Registered Patients</span>
              <span className="kpi-value">{totalPatients}</span>
              <span className="kpi-subtext text-primary">View all patients &rarr;</span>
            </div>

            <div className="kpi-card kpi-emerald cursor-pointer" onClick={() => onNavigate('appointments')}>
              <span className="kpi-label">Active Appointments</span>
              <span className="kpi-value">{scheduledAppts}</span>
              <span className="kpi-subtext">{totalAppointments} Total booked &rarr;</span>
            </div>

            <div className="kpi-card kpi-paid cursor-pointer" onClick={() => onNavigate('billing')}>
              <span className="kpi-label">Revenue Collected</span>
              <span className="kpi-value">${totalRevenue.toFixed(2)}</span>
              <span className="kpi-subtext">{bills.length} Invoices issued &rarr;</span>
            </div>

            <div className="kpi-card kpi-amber cursor-pointer" onClick={() => onNavigate('billing')}>
              <span className="kpi-label">Pending Invoices</span>
              <span className="kpi-value">{pendingBills}</span>
              <span className="kpi-subtext">Awaiting payment &rarr;</span>
            </div>
          </div>

          {/* Quick Actions Bar */}
          <div className="quick-actions-bar">
            <h3 className="section-title">Quick Actions</h3>
            <div className="action-chips">
              <button className="action-chip" onClick={() => onNavigate('patients')}>
                <span className="chip-icon">&#128100;</span> Patient Directory
              </button>
              <button className="action-chip" onClick={() => onNavigate('appointments')}>
                <span className="chip-icon">&#128197;</span> Appointment Calendar
              </button>
              <button className="action-chip" onClick={() => onNavigate('billing')}>
                <span className="chip-icon">&#128179;</span> Invoices &amp; Receipts
              </button>
              <button className="action-chip" onClick={() => onNavigate('reports')}>
                <span className="chip-icon">&#128200;</span> Financial Reports
              </button>
            </div>
          </div>

          {/* Dual Panel: Recent Appointments & Recent Bills */}
          <div className="dashboard-grid-panels">
            {/* Panel 1: Upcoming Appointments */}
            <div className="dashboard-panel">
              <div className="panel-header">
                <h3>Recent &amp; Upcoming Appointments</h3>
                <button className="text-link" onClick={() => onNavigate('appointments')}>
                  View All ({totalAppointments})
                </button>
              </div>

              <div className="panel-body">
                {recentAppointments.length === 0 ? (
                  <div className="empty-state-sm">
                    <p>No appointments booked yet.</p>
                    <button className="btn-primary btn-sm mt-2" onClick={() => onNavigate('appointments')}>
                      Book First Appointment
                    </button>
                  </div>
                ) : (
                  <table className="data-table dashboard-table">
                    <thead>
                      <tr>
                        <th>Date/Time</th>
                        <th>Patient</th>
                        <th>Dentist</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentAppointments.map((a) => (
                        <tr key={a.id}>
                          <td>
                            <strong>{a.appointmentDate}</strong>
                            <div className="text-xs text-muted">{a.appointmentTime}</div>
                          </td>
                          <td>
                            <div className="font-semibold">{a.patientName}</div>
                            <div className="text-xs text-muted">{a.patientNumber}</div>
                          </td>
                          <td>{a.dentistName}</td>
                          <td>
                            <span className={`status-badge status-${a.status.toLowerCase()}`}>
                              {a.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

            {/* Panel 2: Recent Billing & Invoicing */}
            <div className="dashboard-panel">
              <div className="panel-header">
                <h3>Recent Billing Activity</h3>
                <button className="text-link" onClick={() => onNavigate('billing')}>
                  View All ({bills.length})
                </button>
              </div>

              <div className="panel-body">
                {recentBills.length === 0 ? (
                  <div className="empty-state-sm">
                    <p>No billing records found.</p>
                    <button className="btn-primary btn-sm mt-2" onClick={() => onNavigate('billing')}>
                      Issue First Invoice
                    </button>
                  </div>
                ) : (
                  <table className="data-table dashboard-table">
                    <thead>
                      <tr>
                        <th>Bill #</th>
                        <th>Patient</th>
                        <th style={{ textAlign: 'right' }}>Total</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentBills.map((b) => (
                        <tr key={b.id}>
                          <td className="font-semibold text-primary">{b.billNumber}</td>
                          <td>
                            <div>{b.patientName}</div>
                            <div className="text-xs text-muted">{b.billDate}</div>
                          </td>
                          <td style={{ textAlign: 'right', fontWeight: 600 }}>
                            ${Number(b.totalAmount).toFixed(2)}
                          </td>
                          <td>
                            <span className={`status-badge badge-status-${b.status.toLowerCase()}`}>
                              {b.status}
                            </span>
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
