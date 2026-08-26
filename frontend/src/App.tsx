import React, { useState } from 'react';
import './App.css';
import { SystemHealth } from './types';
import { getSystemHealth } from './services/healthService';

export const App: React.FC = () => {
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleCheckHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getSystemHealth();
      setHealth(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to reach backend API');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <span className="badge-milestone">Milestone 0 &bull; Project Foundation</span>
        <h1 className="app-title">Dental Appointment &amp; Patient Management System</h1>
        <p className="app-subtitle">
          University Assessment Project &bull; Architecture and Foundation Baseline
        </p>
      </header>

      <section className="status-card">
        <div className="status-header">
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Backend Connectivity Check</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Verify connectivity with the Spring Boot REST API (<code>/api/v1/health</code>)
            </p>
          </div>
          <button
            className="check-btn"
            onClick={handleCheckHealth}
            disabled={loading}
            id="btn-check-health"
          >
            {loading ? 'Checking...' : 'Check Status'}
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

        {error && (
          <div>
            <div className="status-indicator idle" style={{ marginBottom: '0.75rem' }}>
              <span className="pulse-dot"></span>
              Connection Notice: Backend not reachable or offline
            </div>
            <p style={{ fontSize: '0.85rem', color: '#b45309' }}>{error}</p>
          </div>
        )}
      </section>

      <div className="grid-cards">
        <div className="card">
          <h3 className="card-title">Frontend Stack</h3>
          <p className="card-content">
            Modern Single Page Application structure ready for patient management views and scheduling interfaces.
          </p>
          <ul className="tech-list">
            <li className="tech-tag">React 18</li>
            <li className="tech-tag">TypeScript</li>
            <li className="tech-tag">Vite 5</li>
            <li className="tech-tag">Vanilla CSS</li>
          </ul>
        </div>

        <div className="card">
          <h3 className="card-title">Backend Architecture</h3>
          <p className="card-content">
            Layered architecture with thin REST controllers, decoupled service logic, and Spring Data JPA persistence.
          </p>
          <ul className="tech-list">
            <li className="tech-tag">Java 17 LTS</li>
            <li className="tech-tag">Spring Boot 3.3</li>
            <li className="tech-tag">Apache Maven</li>
            <li className="tech-tag">PostgreSQL</li>
          </ul>
        </div>

        <div className="card">
          <h3 className="card-title">Scope Discipline</h3>
          <p className="card-content">
            Milestone 0 establishes solely the project skeleton and communication channels. No domain features are active yet.
          </p>
          <ul className="tech-list">
            <li className="tech-tag">Zero Mock Auth</li>
            <li className="tech-tag">Zero Fake Data</li>
            <li className="tech-tag">Clean Boundaries</li>
          </ul>
        </div>
      </div>

      <footer className="app-footer">
        Dental Appointment and Patient Management System &bull; Milestone 0 Foundation Verified
      </footer>
    </div>
  );
};

export default App;
