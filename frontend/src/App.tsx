import React, { useState, useEffect } from 'react';
import './App.css';
import { User } from './types';
import { authService } from './services/authService';
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
import { HelpPage } from './pages/HelpPage';

export const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<User | null>(authService.getStoredUser());
  const [activeView, setActiveView] = useState<'login' | 'register' | 'profile' | 'patients' | 'dentists' | 'appointments' | 'billing' | 'reports' | 'dashboard' | 'help'>('login');
  const [notice, setNotice] = useState<string | null>(null);

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

  return (
    <div className="app-layout">
      <Navbar
        currentUser={currentUser}
        activeView={activeView}
        onNavigate={(view) => {
          if (['dashboard', 'profile', 'patients', 'dentists', 'appointments', 'billing', 'reports', 'help'].includes(view) && !currentUser) {
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

        {/* VIEW 5: DENTIST MANAGEMENT */}
        {activeView === 'dentists' && currentUser && (
          <DentistsPage />
        )}

        {/* VIEW 6: APPOINTMENT MANAGEMENT */}
        {activeView === 'appointments' && currentUser && (
          <AppointmentsPage />
        )}

        {/* VIEW 7: BILLING & INVOICING */}
        {activeView === 'billing' && currentUser && (
          <BillingPage />
        )}

        {/* VIEW 8: CLINIC REPORTS */}
        {activeView === 'reports' && currentUser && (
          <ReportsPage />
        )}

        {/* VIEW 9: STAFF HELP & ONBOARDING GUIDE */}
        {activeView === 'help' && currentUser && (
          <HelpPage />
        )}

        {/* VIEW 10: AUTHENTICATED USER PROFILE & SYSTEM STATUS */}
        {activeView === 'profile' && currentUser && (
          <UserProfilePage
            user={currentUser}
            onUserUpdated={setCurrentUser}
            onLogout={handleLogout}
          />
        )}
      </main>

      <footer className="app-footer">
        Dental Appointment and Patient Management System &bull; Enterprise Clinic Management Platform
      </footer>
    </div>
  );
};

export default App;
