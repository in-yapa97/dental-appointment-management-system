import React from 'react';
import { User } from '../types';

interface NavbarProps {
  currentUser: User | null;
  activeView: 'login' | 'register' | 'profile' | 'health';
  onNavigate: (view: 'login' | 'register' | 'profile' | 'health') => void;
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
      <div className="nav-brand" onClick={() => onNavigate(currentUser ? 'profile' : 'login')}>
        <div className="brand-logo">&#10010;</div>
        <span className="brand-title">DentalCare Management</span>
      </div>

      <div className="nav-actions">
        <button
          className={`nav-btn ${activeView === 'health' ? 'active' : ''}`}
          onClick={() => onNavigate('health')}
          id="nav-btn-health"
        >
          System Status
        </button>

        {currentUser ? (
          <>
            <button
              className={`nav-btn ${activeView === 'profile' ? 'active' : ''}`}
              onClick={() => onNavigate('profile')}
              id="nav-btn-profile"
            >
              My Profile
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
