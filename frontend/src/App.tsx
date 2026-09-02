import React, { useState, useEffect } from 'react';
import './App.css';
import { User, SystemHealth } from './types';
import { authService } from './services/authService';
import { getSystemHealth } from './services/healthService';
import { Navbar } from './components/Navbar';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { UserProfilePage } from './pages/UserProfilePage';
import { PatientsPage } from './pages/PatientsPage';
import { DentistsPage } from './pages/DentistsPage';
import { AppointmentsPage } from './pages/AppointmentsPage';
import { BillingPage } from './pages/BillingPage';
import { ReportsPage } from './pages/ReportsPage';
import { DashboardPage } from './pages/DashboardPage';

export const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<User | null>(authService.getStoredUser());
  const [activeView, setActiveView] = useState<'login' | 'register' | 'profile' | 'health' | 'patients' | 'dentists' | 'appointments' | 'billing' | 'reports' | 'dashboard'>('login');
  const [notice, setNotice] = useState<string | null>(null);

  // System Health state (retained from Milestone 0)
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [healthLoading, setHealthLoading] = useState<boolean>(false);
  const [healthError, setHealthError] = useState<string | null>(null);

  useEffect(() => {
    if (authService.isAuthenticated()) {
      authService.getCurrentUser()
        .then((user) => {
          setCurrentUser(user);
          setActiveView('dashboard');
        })
        .catch(() => {
          authService.logout();
          setCurrentUser(null);
          setActiveView('login');
        });
    } else {
      setActiveView('login');
    }
  }, []);

  const handleLoginSuccess = (user: User) => {
    setCurrentUser(user);
    setActiveView('dashboard');
    setNotice(`Welcome back, ${user.fullName}!`);
  };

  const handleRegisterSuccess = (message: string) => {
    setNotice(message);
    setActiveView('login');
  };

  const handleLogout = async () => {
    await authService.logout();
    setCurrentUser(null);
    setActiveView('login');
    setNotice('You have been logged out.');
  };

  const handleCheckHealth = async () => {
    setHealthLoading(true);
    setHealthError(null);
    try {
      const data = await getSystemHealth();
      setHealth(data);
    } catch (err: unknown) {
      setHealthError(err instanceof Error ? err.message : 'Failed to reach backend API');
    } finally {
      setHealthLoading(false);
    }
  };

  return (
    <div className="app-layout">
      <Navbar
        currentUser={currentUser}
        activeView={activeView}
        onNavigate={(view) => {
          if (['dashboard', 'profile', 'patients', 'dentists', 'appointments', 'billing', 'reports'].includes(view) && !currentUser) {
            setActiveView('login');
          } else {
            setActiveView(view);
          }
          setNotice(null);
        }}
        onLogout={handleLogout}
      />

      <main className="main-content">
        {notice && (
          <div className="alert-box alert-success banner-notice">
            <span className="alert-icon">&#10003;</span>
            <span>{notice}</span>
            <button className="close-notice-btn" onClick={() => setNotice(null)}>&times;</button>
          </div>
        )}

        {/* VIEW 1: LOGIN */}
        {activeView === 'login' && !currentUser && (
          <LoginPage
            onLoginSuccess={handleLoginSuccess}
            onNavigateToRegister={() => {
              setActiveView('register');
              setNotice(null);
            }}
          />
        )}

        {/* VIEW 2: REGISTER */}
        {activeView === 'register' && !currentUser && (
          <RegisterPage
            onRegisterSuccess={handleRegisterSuccess}
            onNavigateToLogin={() => {
              setActiveView('login');
              setNotice(null);
            }}
          />
        )}

        {/* VIEW 3: CLINIC DASHBOARD */}
        {activeView === 'dashboard' && currentUser && (
          <DashboardPage
            currentUser={currentUser}
            onNavigate={setActiveView}
          />
        )}

        {/* VIEW 4: PATIENT MANAGEMENT */}
        {activeView === 'patients' && currentUser && (
          <PatientsPage />
        )}

        {/* VIEW 4.5: DENTIST MANAGEMENT */}
        {activeView === 'dentists' && currentUser && (
          <DentistsPage />
        )}

        {/* VIEW 5: APPOINTMENT MANAGEMENT */}
        {activeView === 'appointments' && currentUser && (
          <AppointmentsPage />
        )}

        {/* VIEW 6: BILLING & INVOICING */}
        {activeView === 'billing' && currentUser && (
          <BillingPage />
        )}

        {/* VIEW 7: CLINIC REPORTS */}
        {activeView === 'reports' && currentUser && (
          <ReportsPage />
        )}

        {/* VIEW 8: AUTHENTICATED USER PROFILE */}
        {activeView === 'profile' && currentUser && (
          <UserProfilePage
            user={currentUser}
            onUserUpdated={setCurrentUser}
            onLogout={handleLogout}
          />
        )}

        {/* VIEW 4: SYSTEM HEALTH & ARCHITECTURE (M0 / M1 / M2 status) */}
        {activeView === 'health' && (
          <div className="health-section">
            <section className="status-card">
              <div className="status-header">
                <div>
                  <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Backend Connectivity Check</h2>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                    Verify connectivity with Spring Boot REST API (<code>/api/v1/health</code>)
                  </p>
                </div>
                <button
                  className="check-btn"
                  onClick={handleCheckHealth}
                  disabled={healthLoading}
                  id="btn-check-health"
                >
                  {healthLoading ? 'Checking...' : 'Check Status'}
                </button>
              </div>

              {health && (
                <div>
                  <div className="status-indicator ready" style={{ marginBottom: '0.75rem' }}>
                    <span className="pulse-dot"></span>
                    Backend Status: {health.status}
                  </div>
                  <pre className="response-box">{JSON.stringify(health, null, 2)}</pre>
                </div>
              )}

              {healthError && (
                <div>
                  <div className="status-indicator idle" style={{ marginBottom: '0.75rem' }}>
                    <span className="pulse-dot"></span>
                    Connection Notice: Backend not reachable or offline
                  </div>
                  <p style={{ fontSize: '0.85rem', color: '#b45309' }}>{healthError}</p>
                </div>
              )}
            </section>

            <div className="grid-cards" style={{ marginTop: '1.5rem' }}>
              <div className="card">
                <h3 className="card-title">Milestone 2 Security</h3>
                <p className="card-content">
                  Stateless JWT authentication with HMAC-SHA256, BCrypt password hashing, and role-based access control.
                </p>
                <ul className="tech-list">
                  <li className="tech-tag">Spring Security 6</li>
                  <li className="tech-tag">JJWT 0.12</li>
                  <li className="tech-tag">BCrypt</li>
                  <li className="tech-tag">Stateless Session</li>
                </ul>
              </div>

              <div className="card">
                <h3 className="card-title">Persistence &amp; Domain</h3>
                <p className="card-content">
                  Domain entity model mapping users, patients, dentists, treatments, appointments, and bills in PostgreSQL.
                </p>
                <ul className="tech-list">
                  <li className="tech-tag">PostgreSQL 17</li>
                  <li className="tech-tag">Spring Data JPA</li>
                  <li className="tech-tag">Jakarta Validation</li>
                  <li className="tech-tag">6 Entities</li>
                </ul>
              </div>

              <div className="card">
                <h3 className="card-title">Production Readiness</h3>
                <p className="card-content">
                  Fully integrated clinic platform covering Patients, Appointments, Dentist Availability, Billing, Receipts, and Reports.
                </p>
                <ul className="tech-list">
                  <li className="tech-tag">Stateless JWT</li>
                  <li className="tech-tag">Safe DTOs</li>
                  <li className="tech-tag">Zero Leaked Hashes</li>
                </ul>
              </div>
            </div>
          </div>
        )}
      </main>

      <footer className="app-footer">
        Dental Appointment and Patient Management System &bull; Enterprise Clinic Management Platform
      </footer>
    </div>
  );
};

export default App;
