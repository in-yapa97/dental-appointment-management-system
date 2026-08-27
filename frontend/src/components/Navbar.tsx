import React from 'react';
import { User } from '../types';

interface NavbarProps {
  currentUser: User | null;
  activeView: 'login' | 'register' | 'profile' | 'health' | 'patients' | 'appointments' | 'billing' | 'reports' | 'dashboard';
  onNavigate: (view: 'login' | 'register' | 'profile' | 'health' | 'patients' | 'appointments' | 'billing' | 'reports' | 'dashboard') => void;
  onLogout: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentUser,
  activeView,
  onNavigate,
  onLogout,
}) => {
  return (
    <nav className="navbar">
      <div className="nav-brand" onClick={() => onNavigate(currentUser ? 'dashboard' : 'login')}>
        <div className="brand-logo">&#10010;</div>
        <span className="brand-title">DentalCare Management</span>
      </div>

      <div className="nav-actions">
        {currentUser ? (
          <>
            <button
              className={`nav-btn ${activeView === 'dashboard' ? 'active' : ''}`}
              onClick={() => onNavigate('dashboard')}
              id="nav-btn-dashboard"
            >
              Dashboard
            </button>
            <button
              className={`nav-btn ${activeView === 'patients' ? 'active' : ''}`}
              onClick={() => onNavigate('patients')}
              id="nav-btn-patients"
            >
              Patients
            </button>
            <button
              className={`nav-btn ${activeView === 'appointments' ? 'active' : ''}`}
              onClick={() => onNavigate('appointments')}
              id="nav-btn-appointments"
            >
              Appointments
            </button>
            <button
              className={`nav-btn ${activeView === 'billing' ? 'active' : ''}`}
              onClick={() => onNavigate('billing')}
              id="nav-btn-billing"
            >
              Billing
            </button>
            <button
              className={`nav-btn ${activeView === 'reports' ? 'active' : ''}`}
              onClick={() => onNavigate('reports')}
              id="nav-btn-reports"
            >
              Reports
            </button>
            <button
              className={`nav-btn ${activeView === 'profile' ? 'active' : ''}`}
              onClick={() => onNavigate('profile')}
              id="nav-btn-profile"
            >
              My Profile
            </button>
            <button
              className={`nav-btn ${activeView === 'health' ? 'active' : ''}`}
              onClick={() => onNavigate('health')}
              id="nav-btn-health"
            >
              System Status
            </button>
            <div className="user-pill">
              <span className="user-avatar">{currentUser.fullName.charAt(0).toUpperCase()}</span>
              <div className="user-meta">
                <span className="user-name">{currentUser.fullName}</span>
                <span className="role-badge role-staff">{currentUser.role}</span>
              </div>
            </div>
            <button
              className="logout-btn"
              onClick={onLogout}
              id="nav-btn-logout"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <button
              className={`nav-btn ${activeView === 'login' ? 'active' : ''}`}
              onClick={() => onNavigate('login')}
              id="nav-btn-login"
            >
              Sign In
            </button>
            <button
              className={`nav-btn-primary ${activeView === 'register' ? 'active' : ''}`}
              onClick={() => onNavigate('register')}
              id="nav-btn-register"
            >
              Register
            </button>
          </>
        )}
      </div>
    </nav>
  );
};
