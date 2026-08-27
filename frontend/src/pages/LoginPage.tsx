import React, { useState } from 'react';
import { authService } from '../services/authService';
import { User } from '../types';
import { LogIn, User as UserIcon, Lock, AlertCircle, Activity } from 'lucide-react';

interface LoginPageProps {
  onLoginSuccess: (user: User) => void;
  onNavigateToRegister: () => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({
  onLoginSuccess,
  onNavigateToRegister,
}) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!username.trim()) {
      setError('Username is required');
      return;
    }
    if (!password) {
      setError('Password is required');
      return;
    }

    setLoading(true);
    try {
      const response = await authService.login({ username: username.trim(), password });
      onLoginSuccess(response.user);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <div className="auth-header">
        <div className="brand-logo" style={{ margin: '0 auto 1rem auto', width: '48px', height: '48px' }}>
          <Activity size={24} />
        </div>
        <h2 className="auth-title">Welcome Back</h2>
        <p className="auth-subtitle">Sign in to your DentalCare Management account</p>
      </div>

      {error && (
        <div className="alert-box alert-error" id="login-error-alert" style={{ marginBottom: '1.25rem' }}>
          <AlertCircle size={18} className="alert-icon" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="auth-form" noValidate>
        <div className="form-group">
          <label htmlFor="login-username" className="form-label">Username</label>
          <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
            <UserIcon size={18} style={{ position: 'absolute', left: '0.9rem', color: '#94a3b8' }} />
            <input
              id="login-username"
              type="text"
              className="form-input"
              style={{ paddingLeft: '2.5rem' }}
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              autoFocus
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="login-password" className="form-label">Password</label>
          <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
            <Lock size={18} style={{ position: 'absolute', left: '0.9rem', color: '#94a3b8' }} />
            <input
              id="login-password"
              type="password"
              className="form-input"
              style={{ paddingLeft: '2.5rem' }}
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
          </div>
        </div>

        <button
          type="submit"
          className="submit-btn"
          disabled={loading}
          id="btn-login-submit"
          style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
        >
          <LogIn size={18} />
          <span>{loading ? 'Authenticating...' : 'Sign In'}</span>
        </button>
      </form>

      <div className="auth-footer">
        <span>Don't have an account? </span>
        <button
          type="button"
          className="text-link"
          onClick={onNavigateToRegister}
          id="link-to-register"
        >
          Create an account
        </button>
      </div>
    </div>
  );
};

