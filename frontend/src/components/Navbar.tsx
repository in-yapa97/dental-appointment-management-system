import React from 'react';
import { User } from '../types';
import {
  Activity,
  LayoutDashboard,
  Users,
  Calendar,
  CreditCard,
  BarChart3,
  User as UserIcon,
  ActivitySquare,
  LogOut,
  LogIn,
  UserPlus,
  Stethoscope,
} from 'lucide-react';

interface NavbarProps {
  currentUser: User | null;
  activeView: 'login' | 'register' | 'profile' | 'health' | 'patients' | 'dentists' | 'appointments' | 'billing' | 'reports' | 'dashboard';
  onNavigate: (view: 'login' | 'register' | 'profile' | 'health' | 'patients' | 'dentists' | 'appointments' | 'billing' | 'reports' | 'dashboard') => void;
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
        <div className="brand-logo">
          <Activity size={20} strokeWidth={2.5} />
        </div>
        <div className="brand-text-container">
          <span className="brand-title">DentalCare</span>
          <span className="brand-subtitle">Management System</span>
        </div>
      </div>

      <div className="nav-actions">
        {currentUser ? (
          <>
            <button
              className={`nav-btn ${activeView === 'dashboard' ? 'active' : ''}`}
              onClick={() => onNavigate('dashboard')}
              id="nav-btn-dashboard"
            >
              <LayoutDashboard size={16} />
              <span>Dashboard</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'patients' ? 'active' : ''}`}
              onClick={() => onNavigate('patients')}
              id="nav-btn-patients"
            >
              <Users size={16} />
              <span>Patients</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'dentists' ? 'active' : ''}`}
              onClick={() => onNavigate('dentists')}
              id="nav-btn-dentists"
            >
              <Stethoscope size={16} />
              <span>Dentists</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'appointments' ? 'active' : ''}`}
              onClick={() => onNavigate('appointments')}
              id="nav-btn-appointments"
            >
              <Calendar size={16} />
              <span>Appointments</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'billing' ? 'active' : ''}`}
              onClick={() => onNavigate('billing')}
              id="nav-btn-billing"
            >
              <CreditCard size={16} />
              <span>Billing</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'reports' ? 'active' : ''}`}
              onClick={() => onNavigate('reports')}
              id="nav-btn-reports"
            >
              <BarChart3 size={16} />
              <span>Reports</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'profile' ? 'active' : ''}`}
              onClick={() => onNavigate('profile')}
              id="nav-btn-profile"
            >
              <UserIcon size={16} />
              <span>My Profile</span>
            </button>
            <button
              className={`nav-btn ${activeView === 'health' ? 'active' : ''}`}
              onClick={() => onNavigate('health')}
              id="nav-btn-health"
            >
              <ActivitySquare size={16} />
              <span>System Status</span>
            </button>

            <div className="nav-divider"></div>

            <div className="user-pill" onClick={() => onNavigate('profile')}>
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
              title="Logout"
            >
              <LogOut size={15} />
              <span>Logout</span>
            </button>
          </>
        ) : (
          <>
            <button
              className={`nav-btn ${activeView === 'login' ? 'active' : ''}`}
              onClick={() => onNavigate('login')}
              id="nav-btn-login"
            >
              <LogIn size={16} />
              <span>Sign In</span>
            </button>
            <button
              className={`nav-btn-primary ${activeView === 'register' ? 'active' : ''}`}
              onClick={() => onNavigate('register')}
              id="nav-btn-register"
            >
              <UserPlus size={16} />
              <span>Register</span>
            </button>
          </>
        )}
      </div>
    </nav>
  );
};

