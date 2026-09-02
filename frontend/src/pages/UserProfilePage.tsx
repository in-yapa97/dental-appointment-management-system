import React, { useState, useEffect } from 'react';
import { User, SystemHealth } from '../types';
import { authService } from '../services/authService';
import { getSystemHealth } from '../services/healthService';
import { Activity, RefreshCw, LogOut, Server, ShieldCheck, Database, Layers } from 'lucide-react';

interface UserProfilePageProps {
  user: User;
  onUserUpdated: (user: User) => void;
  onLogout: () => void;
}

export const UserProfilePage: React.FC<UserProfilePageProps> = ({
  user,
  onUserUpdated,
  onLogout,
}) => {
  const [refreshing, setRefreshing] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // System Health Diagnostic State
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [healthLoading, setHealthLoading] = useState<boolean>(false);
  const [healthError, setHealthError] = useState<string | null>(null);

  const handleRefresh = async () => {
    setRefreshing(true);
    setNotice(null);
    setError(null);
    try {
      const updated = await authService.getCurrentUser();
      onUserUpdated(updated);
      setNotice('User profile successfully synchronized with backend (/api/v1/auth/me)');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to refresh profile');
    } finally {
      setRefreshing(false);
    }
  };

  const checkHealth = async () => {
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

  useEffect(() => {
    checkHealth();
  }, []);

  const formattedDate = user.createdAt
    ? new Date(user.createdAt).toLocaleString('en-US', {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
    : 'N/A';

  return (
    <div className="profile-container" style={{ maxWidth: '900px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* 1. USER PROFILE CARD */}
      <div className="profile-card">
        <div className="profile-header">
          <div className="profile-avatar-large">
            {user.fullName.charAt(0).toUpperCase()}
          </div>
          <div className="profile-meta">
            <h2 className="profile-fullname">{user.fullName}</h2>
            <p className="profile-username">@{user.username}</p>
            <div className="profile-badges">
              <span className={`role-badge role-${user.role.toLowerCase()}`}>
                {user.role}
              </span>
              <span className={`status-pill ${user.active ? 'status-active' : 'status-inactive'}`}>
                <span className="dot"></span>
                {user.active ? 'Active Account' : 'Inactive'}
              </span>
            </div>
          </div>
        </div>

        {notice && (
          <div className="alert-box alert-success banner-notice" style={{ margin: '1rem 0' }}>
            <span className="alert-icon">&#10003;</span>
            <span>{notice}</span>
            <button className="close-notice-btn" onClick={() => setNotice(null)}>&times;</button>
          </div>
        )}

        {error && (
          <div className="alert-box alert-error banner-notice" style={{ margin: '1rem 0' }}>
            <span className="alert-icon">&#9888;</span>
            <span>{error}</span>
            <button className="close-notice-btn" onClick={() => setError(null)}>&times;</button>
          </div>
        )}

        <div className="profile-details-grid">
          <div className="detail-item">
            <span className="detail-label">User ID</span>
            <span className="detail-value">#{user.id}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Username</span>
            <span className="detail-value">{user.username}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Assigned Role</span>
            <span className="detail-value">{user.role}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Account Status</span>
            <span className="detail-value">{user.active ? 'Active & Enabled' : 'Disabled'}</span>
          </div>
          <div className="detail-item" style={{ gridColumn: 'span 2' }}>
            <span className="detail-label">Created At</span>
            <span className="detail-value">{formattedDate}</span>
          </div>
        </div>

        <div className="profile-actions">
          <button
            className="check-btn"
            onClick={handleRefresh}
            disabled={refreshing}
            id="btn-refresh-profile"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
          >
            <RefreshCw size={15} className={refreshing ? 'spin' : ''} />
            {refreshing ? 'Syncing...' : 'Sync via /api/v1/auth/me'}
          </button>
          <button
            className="logout-btn"
            onClick={onLogout}
            id="btn-profile-logout"
          >
            <LogOut size={15} />
            Sign Out
          </button>
        </div>
      </div>

      {/* 2. SYSTEM STATUS & BACKEND DIAGNOSTICS */}
      <div className="status-card">
        <div className="status-header">
          <div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Server size={20} className="text-primary" />
              System Status &amp; API Diagnostics
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
              Real-time connectivity and health monitoring for Spring Boot backend &amp; PostgreSQL.
            </p>
          </div>
          <button
            className="check-btn"
            onClick={checkHealth}
            disabled={healthLoading}
            id="btn-check-health"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.85rem' }}
          >
            <Activity size={15} />
            {healthLoading ? 'Testing...' : 'Test /api/v1/health'}
          </button>
        </div>

        {health && (
          <div style={{ marginTop: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <div className="status-indicator ready">
                <span className="pulse-dot"></span>
                API Status: {health.status} (Connected &amp; Healthy)
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                {new Date(health.timestamp).toLocaleTimeString()}
              </span>
            </div>
            <pre className="response-box" style={{ margin: 0 }}>
              {JSON.stringify(health, null, 2)}
            </pre>
          </div>
        )}

        {healthError && (
          <div style={{ marginTop: '1rem' }}>
            <div className="status-indicator idle" style={{ marginBottom: '0.75rem' }}>
              <span className="pulse-dot"></span>
              Connection Notice: Backend not reachable
            </div>
            <p style={{ fontSize: '0.85rem', color: '#b45309' }}>{healthError}</p>
          </div>
        )}

        <div className="grid-cards" style={{ marginTop: '1.5rem' }}>
          <div className="card">
            <h4 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.95rem' }}>
              <ShieldCheck size={16} className="text-primary" />
              Security Architecture
            </h4>
            <p className="card-content" style={{ fontSize: '0.8rem', marginBottom: '0.75rem' }}>
              Stateless JWT authentication with HMAC-SHA256, BCrypt password hashing, and granular Role-Based Access Control.
            </p>
            <ul className="tech-list">
              <li className="tech-tag">Spring Security 6</li>
              <li className="tech-tag">JJWT 0.12</li>
              <li className="tech-tag">BCrypt</li>
            </ul>
          </div>

          <div className="card">
            <h4 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.95rem' }}>
              <Database size={16} className="text-primary" />
              Persistence Layer
            </h4>
            <p className="card-content" style={{ fontSize: '0.8rem', marginBottom: '0.75rem' }}>
              PostgreSQL relational persistence with Spring Data JPA entities, foreign keys, and relational deletion safety.
            </p>
            <ul className="tech-list">
              <li className="tech-tag">PostgreSQL 17</li>
              <li className="tech-tag">Spring Data JPA</li>
              <li className="tech-tag">Hibernate ORM</li>
            </ul>
          </div>

          <div className="card">
            <h4 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.95rem' }}>
              <Layers size={16} className="text-primary" />
              Cloud Infrastructure
            </h4>
            <p className="card-content" style={{ fontSize: '0.8rem', marginBottom: '0.75rem' }}>
              Automated continuous delivery via GitHub Actions, Railway Docker containerization, and Vercel Edge CDN distribution.
            </p>
            <ul className="tech-list">
              <li className="tech-tag">Railway Container</li>
              <li className="tech-tag">Vercel CDN</li>
              <li className="tech-tag">GitHub Actions CI/CD</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
