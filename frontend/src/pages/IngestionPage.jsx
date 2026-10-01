import React, { useState } from 'react';
import { StudyService, PatientService } from '../services/api';
import { UploadCloud, CheckCircle, AlertCircle, FileText, ArrowRight } from 'lucide-react';

export default function IngestionPage({ onStudyCreated }) {
  const [patientId, setPatientId] = useState('');
  const [bodyRegion, setBodyRegion] = useState('CHEST');
  const [modality, setModality] = useState('DX');
  const [clinicalIndication, setClinicalIndication] = useState('Suspected pneumonia with shortness of breath');
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Please select an X-ray image (DICOM, JPEG, or PNG) to upload.');
      return;
    }
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // 1. If no patient ID provided, create a synthetic patient
      let resolvedPatientId = patientId;
      if (!resolvedPatientId) {
        const randId = Math.floor(1000 + Math.random() * 9000);
        const patientRes = await PatientService.createPatient({
          syntheticReference: `SYNTH-PAT-${randId}`,
          ageBand: '45-54',
          sexCode: 'F'
        });
        resolvedPatientId = patientRes.data.patientId;
      }

      // 2. Create the ImagingStudy
      const studyRes = await StudyService.createStudy({
        patientId: resolvedPatientId,
        bodyRegion,
        modality,
        clinicalIndication
      });
      const createdStudyId = studyRes.data.studyId;

      // 3. Upload the image file
      const formData = new FormData();
      formData.append('file', file);
      const uploadRes = await StudyService.uploadImage(createdStudyId, formData);

      // 4. Trigger AI Triage
      await StudyService.runTriage(createdStudyId);

      setSuccess({
        studyId: createdStudyId,
        patientId: resolvedPatientId,
        sha256: uploadRes.data.sha256,
        format: uploadRes.data.format
      });
    } catch (err) {
      setError(err.message || 'Error occurred during ingestion flow.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div className="card">
        <div className="card-header">
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: '700' }}>Study Ingestion &amp; Image Upload</h1>
            <p style={{ fontSize: '0.875rem', color: '#64748b', marginTop: '4px' }}>
              Upload Diagnostic X-Ray (DICOM/JPEG/PNG) to trigger SHA-256 duplicate validation, S3 storage, and AI triage.
            </p>
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

        {success && (
          <div style={{
            padding: '16px',
            backgroundColor: '#dcfce7',
            color: '#15803d',
            borderRadius: '6px',
            marginBottom: '20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '10px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '700' }}>
              <CheckCircle size={20} />
              <span>Study Ingested, Validated, and Triaged Successfully!</span>
            </div>
            <div style={{ fontSize: '0.85rem' }}>
              <strong>Study ID:</strong> {success.studyId} <br />
              <strong>Format:</strong> {success.format} | <strong>SHA-256:</strong> {success.sha256?.substring(0, 24)}...
            </div>
            <button
              onClick={() => onStudyCreated(success.studyId)}
              className="btn-primary"
              style={{ alignSelf: 'flex-start', marginTop: '6px' }}
            >
              <span>View in Study Detail</span>
              <ArrowRight size={16} />
            </button>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
              Patient ID (Optional - leave blank to generate synthetic patient)
            </label>
            <input
              type="text"
              placeholder="e.g. PAT-9281 (or leave blank for auto-generation)"
              value={patientId}
              onChange={(e) => setPatientId(e.target.value)}
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: '6px',
                border: '1px solid #cbd5e1',
                fontSize: '0.875rem'
              }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
                Modality
              </label>
              <select
                value={modality}
                onChange={(e) => setModality(e.target.value)}
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  background: '#ffffff',
                  fontSize: '0.875rem'
                }}
              >
                <option value="DX">Digital Radiography (DX)</option>
                <option value="CR">Computed Radiography (CR)</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
                Body Region
              </label>
              <select
                value={bodyRegion}
                onChange={(e) => setBodyRegion(e.target.value)}
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  background: '#ffffff',
                  fontSize: '0.875rem'
                }}
              >
                <option value="CHEST">CHEST</option>
                <option value="ABDOMEN">ABDOMEN</option>
                <option value="EXTREMITY">EXTREMITY</option>
                <option value="SPINE">SPINE</option>
              </select>
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
              Clinical Indication
            </label>
            <input
              type="text"
              value={clinicalIndication}
              onChange={(e) => setClinicalIndication(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: '6px',
                border: '1px solid #cbd5e1',
                fontSize: '0.875rem'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
              X-Ray File (DICOM .dcm / .ima or JPEG / PNG)
            </label>
            <input
              type="file"
              accept=".dcm,.ima,image/jpeg,image/png"
              onChange={(e) => setFile(e.target.files[0] || null)}
              required
              style={{
                display: 'block',
                width: '100%',
                padding: '12px',
                border: '2px dashed #cbd5e1',
                borderRadius: '8px',
                cursor: 'pointer',
                backgroundColor: '#f8fafc'
              }}
            />
            <p style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '6px' }}>
              Max file size: 50MB. DICOM is parsed for Study/Series/SOP UID and acquisition metadata.
            </p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn-primary"
            style={{ padding: '12px', justifyContent: 'center', fontSize: '1rem', marginTop: '8px' }}
          >
            <UploadCloud size={20} />
            <span>{loading ? 'Ingesting, Validating & Triaging...' : 'Submit & Ingest Study'}</span>
          </button>
        </form>
      </div>
    </div>
  );
}
