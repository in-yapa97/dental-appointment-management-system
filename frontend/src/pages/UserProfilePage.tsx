import React, { useState } from 'react';
import { User } from '../types';
import { authService } from '../services/authService';

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

  const formattedDate = user.createdAt
    ? new Date(user.createdAt).toLocaleString('en-US', {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
    : 'N/A';

  return (
    <div className="profile-container">
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
          <div className="alert-box alert-success" style={{ margin: '1rem 0' }}>
            <span className="alert-icon">&#10003;</span>
            <span>{notice}</span>
          </div>
        )}

        {error && (
          <div className="alert-box alert-error" style={{ margin: '1rem 0' }}>
            <span className="alert-icon">&#9888;</span>
            <span>{error}</span>
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
          >
            {refreshing ? 'Syncing...' : 'Sync via /api/v1/auth/me'}
          </button>
          <button
            className="logout-btn"
            onClick={onLogout}
            id="btn-profile-logout"
          >
            Sign Out
          </button>
        </div>
      </div>
    </div>
  );
};
