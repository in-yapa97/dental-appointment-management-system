import React, { useState, useEffect, useCallback } from 'react';
import { Appointment, Bill, Patient, User } from '../types';
import { patientService } from '../services/patientService';
import { appointmentService } from '../services/appointmentService';
import { billingService } from '../services/billingService';
import {
  Users,
  Calendar,
  DollarSign,
  Clock,
  ArrowRight,
  BarChart3,
  CalendarCheck,
  UserPlus,
  Receipt,
  Sparkles,
  Stethoscope,
  BookOpen,
} from 'lucide-react';

interface DashboardPageProps {
  currentUser: User;
  onNavigate: (view: 'dashboard' | 'patients' | 'dentists' | 'appointments' | 'billing' | 'reports' | 'help') => void;
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
          <div className="hero-badge">
            <Sparkles size={14} className="hero-badge-icon" />
            <span>Clinic Executive Portal</span>
          </div>
          <h1 className="hero-title">Welcome back, {currentUser.fullName}</h1>
          <p className="hero-subtitle">
            DentalCare Management Dashboard &bull; {todayFormatted}
          </p>
        </div>
        <div className="hero-actions">
          <button className="btn-primary" onClick={() => onNavigate('appointments')}>
            <CalendarCheck size={16} />
            <span>Book Appointment</span>
          </button>
          <button className="btn-secondary" onClick={() => onNavigate('patients')}>
            <UserPlus size={16} />
            <span>Add Patient</span>
          </button>
          <button className="btn-secondary" onClick={() => onNavigate('help')} title="Open Staff Help Guide">
            <BookOpen size={16} />
            <span>Staff Guide</span>
          </button>
        </div>
      </div>

      {error && <div className="banner-error mb-4">{error}</div>}

      {loading ? (
        <div className="table-loading">
          <div className="spinner"></div>
          <span>Loading clinic metrics...</span>
        </div>
      ) : (
        <>
          {/* Quick Metrics KPI Cards */}
          <div className="dashboard-kpi-grid">
            <div className="kpi-card kpi-indigo cursor-pointer" onClick={() => onNavigate('patients')}>
              <div className="kpi-top">
                <span className="kpi-label">Registered Patients</span>
                <div className="kpi-icon-box icon-indigo">
                  <Users size={20} />
                </div>
              </div>
              <span className="kpi-value">{totalPatients}</span>
              <div className="kpi-footer">
                <span className="kpi-subtext text-primary">View patient records</span>
                <ArrowRight size={14} className="kpi-arrow" />
              </div>
            </div>

            <div className="kpi-card kpi-emerald cursor-pointer" onClick={() => onNavigate('appointments')}>
              <div className="kpi-top">
                <span className="kpi-label">Active Appointments</span>
                <div className="kpi-icon-box icon-emerald">
                  <Calendar size={20} />
                </div>
              </div>
              <span className="kpi-value">{scheduledAppts}</span>
              <div className="kpi-footer">
                <span className="kpi-subtext">{totalAppointments} total booked</span>
                <ArrowRight size={14} className="kpi-arrow" />
              </div>
            </div>

            <div className="kpi-card kpi-paid cursor-pointer" onClick={() => onNavigate('billing')}>
              <div className="kpi-top">
                <span className="kpi-label">Revenue Collected</span>
                <div className="kpi-icon-box icon-sky">
                  <DollarSign size={20} />
                </div>
              </div>
              <span className="kpi-value">${totalRevenue.toFixed(2)}</span>
              <div className="kpi-footer">
                <span className="kpi-subtext">{bills.length} invoices issued</span>
                <ArrowRight size={14} className="kpi-arrow" />
              </div>
            </div>

            <div className="kpi-card kpi-amber cursor-pointer" onClick={() => onNavigate('billing')}>
              <div className="kpi-top">
                <span className="kpi-label">Pending Invoices</span>
                <div className="kpi-icon-box icon-amber">
                  <Clock size={20} />
                </div>
              </div>
              <span className="kpi-value">{pendingBills}</span>
              <div className="kpi-footer">
                <span className="kpi-subtext">Awaiting payment</span>
                <ArrowRight size={14} className="kpi-arrow" />
              </div>
            </div>
          </div>

          {/* Quick Actions Bar */}
          <div className="quick-actions-bar">
            <h3 className="section-title">Quick Actions</h3>
            <div className="action-chips">
              <button className="action-chip" onClick={() => onNavigate('patients')}>
                <Users size={16} className="chip-icon" />
                <span>Patient Directory</span>
              </button>
              <button className="action-chip" onClick={() => onNavigate('dentists')}>
                <Stethoscope size={16} className="chip-icon" />
                <span>Dentist Directory</span>
              </button>
              <button className="action-chip" onClick={() => onNavigate('appointments')}>
                <Calendar size={16} className="chip-icon" />
                <span>Appointment Calendar</span>
              </button>
              <button className="action-chip" onClick={() => onNavigate('billing')}>
                <Receipt size={16} className="chip-icon" />
                <span>Invoices &amp; Receipts</span>
              </button>
              <button className="action-chip" onClick={() => onNavigate('reports')}>
                <BarChart3 size={16} className="chip-icon" />
                <span>Financial Reports</span>
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

