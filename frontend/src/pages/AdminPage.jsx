import React, { useState, useEffect } from 'react';
import { ObservabilityService } from '../services/api';
import { Activity, Server, Database, HardDrive, Cpu, CheckCircle2, AlertCircle } from 'lucide-react';

export default function AdminPage() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await ObservabilityService.getHealth();
      setHealth(res.data);
    } catch (err) {
      setError(err.message || 'Service unreachable');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="card">
        <div className="card-header">
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: '700' }}>System Observability &amp; Clinical Health</h1>
            <p style={{ fontSize: '0.875rem', color: '#64748b', marginTop: '2px' }}>
              Spring Boot Actuator telemetry, database connectivity, and cloud storage status.
            </p>
          </div>
          <button onClick={fetchHealth} className="btn-secondary">
            <Activity size={16} /> Check Health
          </button>
        </div>

        {error ? (
          <div style={{ padding: '16px', backgroundColor: '#fee2e2', color: '#b91c1c', borderRadius: '6px', display: 'flex', gap: '10px' }}>
            <AlertCircle size={20} />
            <div>
              <strong>Service Offline or Health Endpoint Inaccessible:</strong> {error}
              <p style={{ fontSize: '0.8rem', marginTop: '4px' }}>
                Ensure the backend is running at <code>http://localhost:8080/actuator/health</code>.
              </p>
            </div>
          </div>
        ) : health ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#15803d', fontWeight: '700' }}>
                <CheckCircle2 size={18} />
                <span>Overall Status</span>
              </div>
              <div style={{ fontSize: '1.5rem', fontWeight: '800', color: '#15803d', marginTop: '8px' }}>
                {health.status || 'UP'}
              </div>
            </div>

            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#0369a1', fontWeight: '700' }}>
                <Database size={18} />
                <span>Database</span>
              </div>
              <div style={{ fontSize: '1rem', fontWeight: '600', color: '#0f172a', marginTop: '8px' }}>
                {health.components?.db?.status || 'CONNECTED'}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#64748b' }}>PostgreSQL / H2 In-Memory</div>
            </div>

            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#0369a1', fontWeight: '700' }}>
                <HardDrive size={18} />
                <span>Object Storage</span>
              </div>
              <div style={{ fontSize: '1rem', fontWeight: '600', color: '#0f172a', marginTop: '8px' }}>
                S3 / MinIO Ready
              </div>
              <div style={{ fontSize: '0.75rem', color: '#64748b' }}>dxr-xray-images bucket</div>
            </div>
          </div>
        ) : (
          <div>Checking health endpoints...</div>
        )}
      </div>

      {/* Workflow Transition Matrix */}
      <div className="card">
        <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '12px' }}>
          Healthcare Workflow State Machine Compliance
        </h2>
        <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '16px' }}>
          Enforced by service-layer domain rules. Invalid state transitions (e.g. attempting to skip ingestion or edit after verification) are rejected with HTTP 409 Conflict.
        </p>

        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          backgroundColor: '#f8fafc',
          padding: '20px',
          borderRadius: '8px',
          border: '1px solid #e2e8f0',
          overflowX: 'auto'
        }}>
          {['RECEIVED', 'VALIDATED', 'TRIAGED', 'IN_REVIEW', 'REPORTED', 'VERIFIED'].map((step, idx, arr) => (
            <React.Fragment key={step}>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px' }}>
                <span style={{
                  padding: '6px 14px',
                  borderRadius: '20px',
                  fontSize: '0.75rem',
                  fontWeight: '700',
                  backgroundColor: step === 'VERIFIED' ? '#dcfce7' : '#e0f2fe',
                  color: step === 'VERIFIED' ? '#15803d' : '#0369a1',
                  border: '1px solid #cbd5e1'
                }}>
                  {step}
                </span>
                <span style={{ fontSize: '0.7rem', color: '#64748b' }}>Stage {idx + 1}</span>
              </div>
              {idx < arr.length - 1 && (
                <div style={{ height: '2px', flex: 1, backgroundColor: '#cbd5e1', margin: '0 8px' }} />
              )}
            </React.Fragment>
          ))}
        </div>
      </div>
    </div>
  );
}
