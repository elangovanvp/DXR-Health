import React, { useState, useEffect } from 'react';
import { WorklistService } from '../services/api';
import PriorityBadge from '../components/PriorityBadge';
import StatusBadge from '../components/StatusBadge';
import { AlertCircle, RefreshCw, Filter, Eye, Clock, CheckCircle2 } from 'lucide-react';

export default function WorklistPage({ onSelectStudy }) {
  const [studies, setStudies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filterPriority, setFilterPriority] = useState('');

  const fetchWorklist = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {};
      if (filterPriority) params.priority = filterPriority;
      const res = await WorklistService.getWorklist(params);
      setStudies(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch worklist');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorklist();
  }, [filterPriority]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div className="card">
        <div className="card-header">
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: '700', color: '#0f172a' }}>
              Radiologist Priority Worklist
            </h1>
            <p style={{ fontSize: '0.875rem', color: '#64748b', marginTop: '4px' }}>
              Studies automatically prioritized by AI triage layer with graceful chronological fallback.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.875rem', color: '#475569' }}>
              <Filter size={16} />
              <span>Priority:</span>
              <select
                value={filterPriority}
                onChange={(e) => setFilterPriority(e.target.value)}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  background: '#ffffff',
                  fontSize: '0.875rem'
                }}
              >
                <option value="">All Priorities</option>
                <option value="CRITICAL">CRITICAL</option>
                <option value="HIGH">HIGH</option>
                <option value="ROUTINE">ROUTINE</option>
              </select>
            </div>

            <button
              onClick={fetchWorklist}
              className="btn-secondary"
              title="Refresh Worklist"
            >
              <RefreshCw size={16} className={loading ? 'spin' : ''} />
              Refresh
            </button>
          </div>
        </div>

        {error && (
          <div style={{
            padding: '12px 16px',
            backgroundColor: '#fee2e2',
            color: '#b91c1c',
            borderRadius: '6px',
            marginBottom: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <div className="table-container">
          <table className="clinical-table">
            <thead>
              <tr>
                <th>Priority</th>
                <th>Study ID</th>
                <th>Patient ID</th>
                <th>Modality / Region</th>
                <th>Clinical Indication</th>
                <th>Workflow Status</th>
                <th>Confidence</th>
                <th>Candidate Labels</th>
                <th>Acquired / Created</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="10" style={{ textAlign: 'center', padding: '36px', color: '#64748b' }}>
                    Loading clinical worklist...
                  </td>
                </tr>
              ) : studies.length === 0 ? (
                <tr>
                  <td colSpan="10" style={{ textAlign: 'center', padding: '36px', color: '#64748b' }}>
                    No unread studies matching criteria in worklist.
                  </td>
                </tr>
              ) : (
                studies.map((item) => (
                  <tr key={item.studyId}>
                    <td>
                      <PriorityBadge priority={item.priority} />
                    </td>
                    <td style={{ fontFamily: 'monospace', fontWeight: '600' }}>
                      {item.studyId}
                    </td>
                    <td>{item.patientReference || item.patientId}</td>
                    <td>
                      <span style={{ fontWeight: '600', color: '#0284c7' }}>{item.modality}</span> &bull; {item.bodyRegion}
                    </td>
                    <td>{item.clinicalIndication}</td>
                    <td>
                      <StatusBadge status={item.workflowStatus} />
                    </td>
                    <td>
                      {item.confidence ? `${(item.confidence * 100).toFixed(1)}%` : '—'}
                    </td>
                    <td>
                      {item.candidateLabels && item.candidateLabels.length > 0 ? (
                        <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                          {item.candidateLabels.map((lbl, idx) => (
                            <span
                              key={idx}
                              style={{
                                fontSize: '0.7rem',
                                backgroundColor: '#f1f5f9',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                border: '1px solid #e2e8f0'
                              }}
                            >
                              {lbl}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span style={{ color: '#94a3b8' }}>None</span>
                      )}
                    </td>
                    <td style={{ fontSize: '0.8rem', color: '#64748b' }}>
                      {item.createdAt ? new Date(item.createdAt).toLocaleString() : '—'}
                    </td>
                    <td>
                      <button
                        onClick={() => onSelectStudy(item.studyId)}
                        className="btn-primary"
                        style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                      >
                        <Eye size={14} />
                        Review
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
