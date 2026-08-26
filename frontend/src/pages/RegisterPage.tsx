import React, { useState } from 'react';
import { authService } from '../services/authService';

interface RegisterPageProps {
  onRegisterSuccess: (message: string) => void;
  onNavigateToLogin: () => void;
}

export const RegisterPage: React.FC<RegisterPageProps> = ({
  onRegisterSuccess,
  onNavigateToLogin,
}) => {
  const [username, setUsername] = useState('');
  const [fullName, setFullName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!username.trim()) {
      setError('Username is required');
      return;
    }
    if (username.trim().length < 3) {
      setError('Username must be at least 3 characters');
      return;
    }
    if (!fullName.trim()) {
      setError('Full name is required');
      return;
    }
    if (!password) {
      setError('Password is required');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      await authService.register({
        username: username.trim(),
        fullName: fullName.trim(),
        password,
      });
      onRegisterSuccess(`User '${username.trim()}' registered successfully! You can now log in.`);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <div className="auth-header">
        <h2 className="auth-title">Create Account</h2>
        <p className="auth-subtitle">Register a new staff account in the Dental System</p>
      </div>

      {error && (
        <div className="alert-box alert-error" id="register-error-alert">
          <span className="alert-icon">&#9888;</span>
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="auth-form" noValidate>
        <div className="form-group">
          <label htmlFor="reg-username" className="form-label">Username</label>
          <input
            id="reg-username"
            type="text"
            className="form-input"
            placeholder="e.g. jdoe (min. 3 characters)"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={loading}
            autoFocus
          />
        </div>

        <div className="form-group">
          <label htmlFor="reg-fullname" className="form-label">Full Name</label>
          <input
            id="reg-fullname"
            type="text"
            className="form-input"
            placeholder="e.g. Dr. John Doe or Jane Smith"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="reg-password" className="form-label">Password</label>
          <input
            id="reg-password"
            type="password"
            className="form-input"
            placeholder="Min. 6 characters"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="reg-confirm" className="form-label">Confirm Password</label>
          <input
            id="reg-confirm"
            type="password"
            className="form-input"
            placeholder="Re-enter your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={loading}
          />
        </div>

        <button
          type="submit"
          className="submit-btn"
          disabled={loading}
          id="btn-register-submit"
        >
          {loading ? 'Creating Account...' : 'Register Account'}
        </button>
      </form>

      <div className="auth-footer">
        <span>Already have an account? </span>
        <button
          type="button"
          className="text-link"
          onClick={onNavigateToLogin}
          id="link-to-login"
        >
          Sign In
        </button>
      </div>
    </div>
  );
};
