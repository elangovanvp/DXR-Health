import React, { useState, useEffect } from 'react';
import { PatientService } from '../services/api';
import StatusBadge from '../components/StatusBadge';
import { Users, Search, PlusCircle, Calendar, Eye } from 'lucide-react';

export default function PatientPage({ onSelectStudy }) {
  const [patients, setPatients] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [patientStudies, setPatientStudies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [newRef, setNewRef] = useState('');
  const [newAgeBand, setNewAgeBand] = useState('45-54');
  const [newSexCode, setNewSexCode] = useState('M');

  const fetchPatients = async () => {
    try {
      const res = await PatientService.listPatients();
      setPatients(res.data || []);
      if (res.data?.length > 0 && !selectedPatientId) {
        setSelectedPatientId(res.data[0].patientId);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchPatientStudies = async (id) => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await PatientService.getPatientStudies(id);
      setPatientStudies(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPatients();
  }, []);

  useEffect(() => {
    if (selectedPatientId) {
      fetchPatientStudies(selectedPatientId);
    }
  }, [selectedPatientId]);

  const handleCreatePatient = async (e) => {
    e.preventDefault();
    if (!newRef.trim()) return;
    try {
      const res = await PatientService.createPatient({
        syntheticReference: newRef,
        ageBand: newAgeBand,
        sexCode: newSexCode
      });
      setNewRef('');
      await fetchPatients();
      setSelectedPatientId(res.data.patientId);
    } catch (err) {
      alert(err.message || 'Failed to create patient');
    }
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '24px' }}>
      {/* Left Column: Create Patient & Patient List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <div className="card">
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '14px' }}>
            Register Synthetic Patient
          </h2>
          <form onSubmit={handleCreatePatient} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div>
              <label style={{ fontSize: '0.8rem', fontWeight: '600', color: '#475569' }}>
                Synthetic Identifier
              </label>
              <input
                type="text"
                placeholder="e.g. SYNTH-PAT-0042"
                value={newRef}
                onChange={(e) => setNewRef(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '8px 10px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  marginTop: '4px'
                }}
              />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: '#475569' }}>Age Band</label>
                <select
                  value={newAgeBand}
                  onChange={(e) => setNewAgeBand(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px',
                    borderRadius: '6px',
                    border: '1px solid #cbd5e1',
                    background: '#ffffff',
                    marginTop: '4px'
                  }}
                >
                  <option value="18-29">18-29</option>
                  <option value="30-44">30-44</option>
                  <option value="45-54">45-54</option>
                  <option value="55-64">55-64</option>
                  <option value="65+">65+</option>
                </select>
              </div>
              <div>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: '#475569' }}>Sex</label>
                <select
                  value={newSexCode}
                  onChange={(e) => setNewSexCode(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px',
                    borderRadius: '6px',
                    border: '1px solid #cbd5e1',
                    background: '#ffffff',
                    marginTop: '4px'
                  }}
                >
                  <option value="M">Male (M)</option>
                  <option value="F">Female (F)</option>
                  <option value="O">Other (O)</option>
                </select>
              </div>
            </div>
            <button type="submit" className="btn-primary" style={{ justifyContent: 'center', marginTop: '6px' }}>
              <PlusCircle size={16} />
              <span>Create Patient</span>
            </button>
          </form>
        </div>

        <div className="card">
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '14px' }}>
            Registered Patients ({patients.length})
          </h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', maxHeight: '400px', overflowY: 'auto' }}>
            {patients.map((p) => {
              const isSelected = p.patientId === selectedPatientId;
              return (
                <div
                  key={p.patientId}
                  onClick={() => setSelectedPatientId(p.patientId)}
                  style={{
                    padding: '10px 12px',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    backgroundColor: isSelected ? '#e0f2fe' : '#f8fafc',
                    border: `1px solid ${isSelected ? '#0284c7' : '#e2e8f0'}`,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <div>
                    <strong style={{ fontSize: '0.875rem' }}>{p.syntheticReference}</strong>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
                      {p.ageBand} &bull; {p.sexCode} &bull; {p.patientId.substring(0, 8)}...
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Right Column: Imaging History for Selected Patient */}
      <div className="card">
        <div className="card-header">
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Patient Imaging History</h2>
            <p style={{ fontSize: '0.85rem', color: '#64748b', marginTop: '2px' }}>
              Sequential imaging history for selected synthetic patient context.
            </p>
          </div>
        </div>

        <div className="table-container">
          <table className="clinical-table">
            <thead>
              <tr>
                <th>Study ID</th>
                <th>Modality</th>
                <th>Body Region</th>
                <th>Indication</th>
                <th>Status</th>
                <th>Date</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '30px' }}>Loading patient studies...</td>
                </tr>
              ) : patientStudies.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '30px', color: '#64748b' }}>
                    No imaging studies recorded for this patient.
                  </td>
                </tr>
              ) : (
                patientStudies.map((s) => (
                  <tr key={s.studyId}>
                    <td style={{ fontFamily: 'monospace', fontWeight: '600' }}>{s.studyId}</td>
                    <td>{s.modality}</td>
                    <td>{s.bodyRegion}</td>
                    <td>{s.clinicalIndication}</td>
                    <td><StatusBadge status={s.workflowStatus} /></td>
                    <td style={{ fontSize: '0.8rem', color: '#64748b' }}>
                      {new Date(s.createdAt).toLocaleDateString()}
                    </td>
                    <td>
                      <button
                        onClick={() => onSelectStudy(s.studyId)}
                        className="btn-primary"
                        style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                      >
                        <Eye size={13} />
                        View
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
