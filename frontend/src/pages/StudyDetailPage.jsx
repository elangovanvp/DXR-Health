import React, { useState, useEffect } from 'react';
import { StudyService } from '../services/api';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import { ArrowLeft, RefreshCw, CheckCircle, AlertTriangle, ShieldCheck, Lock, Play } from 'lucide-react';

export default function StudyDetailPage({ studyId, onBack }) {
  const [study, setStudy] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reportActionLoading, setReportActionLoading] = useState(false);

  // Diagnostic Report form state
  const [findings, setFindings] = useState('');
  const [impression, setImpression] = useState('');
  const [authorRef, setAuthorRef] = useState('Dr. Sarah Chen, MD (Radiology)');
  const [message, setMessage] = useState(null);

  const fetchStudyDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await StudyService.getStudy(studyId);
      setStudy(res.data);
      if (res.data.report) {
        setFindings(res.data.report.findings || '');
        setImpression(res.data.report.impression || '');
        setAuthorRef(res.data.report.authorRef || 'Dr. Sarah Chen, MD (Radiology)');
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch study details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (studyId) fetchStudyDetails();
  }, [studyId]);

  const handleClaimReview = async () => {
    try {
      setReportActionLoading(true);
      await StudyService.updateStatus(studyId, { targetStatus: 'IN_REVIEW' });
      await fetchStudyDetails();
      setMessage({ type: 'success', text: 'Study claimed and marked IN_REVIEW.' });
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to transition to IN_REVIEW.' });
    } finally {
      setReportActionLoading(false);
    }
  };

  const handleSaveReportDraft = async (e) => {
    e.preventDefault();
    try {
      setReportActionLoading(true);
      await StudyService.submitReport(studyId, {
        findings,
        impression,
        authorRef,
        verified: false
      });
      await fetchStudyDetails();
      setMessage({ type: 'success', text: 'Draft report saved successfully.' });
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to save report.' });
    } finally {
      setReportActionLoading(false);
    }
  };

  const handleVerifyReport = async () => {
    if (!findings.trim() || !impression.trim()) {
      setMessage({ type: 'error', text: 'Findings and Impression are required to verify the report.' });
      return;
    }
    if (!window.confirm('Are you sure you want to verify and finalize this diagnostic report? Once verified, it becomes permanent and immutable.')) {
      return;
    }
    try {
      setReportActionLoading(true);
      await StudyService.submitReport(studyId, {
        findings,
        impression,
        authorRef,
        verified: true
      });
      await fetchStudyDetails();
      setMessage({ type: 'success', text: 'Diagnostic report VERIFIED and signed into permanent medical record.' });
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to verify report.' });
    } finally {
      setReportActionLoading(false);
    }
  };

  const handleReRunTriage = async () => {
    try {
      setReportActionLoading(true);
      await StudyService.runTriage(studyId);
      await fetchStudyDetails();
      setMessage({ type: 'success', text: 'AI Triage re-evaluated successfully.' });
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to re-run triage.' });
    } finally {
      setReportActionLoading(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Loading study context...</div>;
  }

  if (error || !study) {
    return (
      <div className="card">
        <div style={{ color: '#b91c1c', marginBottom: '16px' }}>{error || 'Study not found'}</div>
        <button onClick={onBack} className="btn-secondary">
          <ArrowLeft size={16} /> Back to Worklist
        </button>
      </div>
    );
  }

  const isVerified = study.workflowStatus === 'VERIFIED';
  const triage = study.triageResult;
  const image = study.images && study.images.length > 0 ? study.images[0] : null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <button onClick={onBack} className="btn-secondary">
          <ArrowLeft size={16} />
          <span>Back to Worklist</span>
        </button>

        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <StatusBadge status={study.workflowStatus} />
          {triage && <PriorityBadge priority={triage.priority} />}
        </div>
      </div>

      {message && (
        <div style={{
          padding: '12px 16px',
          borderRadius: '6px',
          backgroundColor: message.type === 'success' ? '#dcfce7' : '#fee2e2',
          color: message.type === 'success' ? '#15803d' : '#b91c1c',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          {message.type === 'success' ? <CheckCircle size={18} /> : <AlertTriangle size={18} />}
          <span>{message.text}</span>
        </div>
      )}

      {/* Main Grid: Left Column Context, Right Column Reporting */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        
        {/* Left Column: Study, Image, and Triage */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="card">
            <h2 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '16px' }}>Study Information</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '0.875rem' }}>
              <div>
                <span style={{ color: '#64748b', display: 'block' }}>Study ID:</span>
                <strong style={{ fontFamily: 'monospace' }}>{study.studyId}</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', display: 'block' }}>Patient ID:</span>
                <strong>{study.patientReference || study.patientId}</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', display: 'block' }}>Modality &amp; Region:</span>
                <strong>{study.modality} &bull; {study.bodyRegion}</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', display: 'block' }}>Created At:</span>
                <span>{new Date(study.createdAt).toLocaleString()}</span>
              </div>
            </div>
            <div style={{ marginTop: '12px', fontSize: '0.875rem' }}>
              <span style={{ color: '#64748b', display: 'block' }}>Clinical Indication:</span>
              <p style={{ marginTop: '4px', padding: '8px', backgroundColor: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
                {study.clinicalIndication}
              </p>
            </div>
          </div>

          {/* AI-Ready Triage Box */}
          <div className="card" style={{ borderLeft: '4px solid #0284c7' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>AI Triage Decision Support</h2>
              {!isVerified && (
                <button
                  onClick={handleReRunTriage}
                  disabled={reportActionLoading}
                  className="btn-secondary"
                  style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                >
                  <Play size={12} /> Re-run Triage
                </button>
              )}
            </div>

            {triage ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.875rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>Assigned Operational Priority:</span>
                  <PriorityBadge priority={triage.priority} />
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Inference Confidence:</span>
                  <strong>{triage.confidence ? `${(triage.confidence * 100).toFixed(1)}%` : 'N/A'}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Adapter &amp; Model Version:</span>
                  <span style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                    {triage.modelName} (v{triage.modelVersion})
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Telemetry Processing Time:</span>
                  <span>{triage.processingTimeMs} ms</span>
                </div>

                {triage.candidateLabels && triage.candidateLabels.length > 0 && (
                  <div style={{ marginTop: '6px' }}>
                    <span style={{ color: '#64748b', display: 'block', marginBottom: '6px' }}>Candidate Findings:</span>
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                      {triage.candidateLabels.map((lbl, i) => (
                        <span
                          key={i}
                          style={{
                            fontSize: '0.75rem',
                            backgroundColor: '#fef3c7',
                            color: '#92400e',
                            padding: '3px 8px',
                            borderRadius: '4px',
                            border: '1px solid #fde68a',
                            fontWeight: '600'
                          }}
                        >
                          {lbl}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                <div style={{ marginTop: '8px', fontSize: '0.75rem', color: '#64748b', fontStyle: 'italic' }}>
                  * AI triage output is operational prioritization signal only. Final clinical interpretation requires radiologist evaluation.
                </div>
              </div>
            ) : (
              <div style={{ color: '#64748b', fontSize: '0.875rem' }}>
                No triage record found. Upload image and run triage.
              </div>
            )}
          </div>

          {/* Stored Image Asset */}
          <div className="card">
            <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '14px' }}>Imaging Asset</h2>
            {image ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.875rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Format:</span>
                  <strong>{image.format}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>File Size:</span>
                  <span>{(image.sizeBytes / 1024).toFixed(1)} KB</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Dimensions:</span>
                  <span>{image.rows && image.columns ? `${image.columns} x ${image.rows} px` : 'Preserved'}</span>
                </div>
                <div>
                  <span style={{ color: '#64748b', display: 'block' }}>SHA-256 Digest:</span>
                  <div style={{
                    fontFamily: 'monospace',
                    fontSize: '0.75rem',
                    backgroundColor: '#f1f5f9',
                    padding: '6px 8px',
                    borderRadius: '4px',
                    wordBreak: 'break-all',
                    marginTop: '4px'
                  }}>
                    {image.sha256}
                  </div>
                </div>
              </div>
            ) : (
              <div style={{ color: '#64748b', fontSize: '0.875rem' }}>
                No image asset attached to this study.
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Radiologist Report & Verification */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div className="card-header">
            <div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Radiology Diagnostic Report</h2>
              <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '2px' }}>
                Human-authored clinical interpretation adhering to standard reporting workflow.
              </p>
            </div>
            {study.workflowStatus === 'TRIAGED' && (
              <button
                onClick={handleClaimReview}
                disabled={reportActionLoading}
                className="btn-primary"
                style={{ fontSize: '0.8rem' }}
              >
                Claim (IN_REVIEW)
              </button>
            )}
          </div>

          {isVerified && (
            <div style={{
              padding: '12px 16px',
              backgroundColor: '#dcfce7',
              color: '#166534',
              borderRadius: '6px',
              marginBottom: '16px',
              display: 'flex',
              alignItems: 'center',
              gap: '10px'
            }}>
              <ShieldCheck size={22} />
              <div>
                <strong>Report Finalized &amp; Verified</strong>
                <p style={{ fontSize: '0.8rem' }}>
                  Verified by {study.report?.authorRef} on {new Date(study.report?.verifiedAt).toLocaleString()}.
                  This document is locked and immutable.
                </p>
              </div>
            </div>
          )}

          <form onSubmit={handleSaveReportDraft} style={{ display: 'flex', flexDirection: 'column', gap: '16px', flex: 1 }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
                Reporting Radiologist
              </label>
              <input
                type="text"
                value={authorRef}
                onChange={(e) => setAuthorRef(e.target.value)}
                disabled={isVerified}
                style={{
                  width: '100%',
                  padding: '8px 12px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  backgroundColor: isVerified ? '#f8fafc' : '#ffffff',
                  fontSize: '0.875rem'
                }}
              />
            </div>

            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
                Findings
              </label>
              <textarea
                rows={6}
                value={findings}
                onChange={(e) => setFindings(e.target.value)}
                placeholder="Enter detailed radiological findings (e.g. Lung volumes, parenchymal opacities, pleural spaces, cardiac silhouette)..."
                disabled={isVerified}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  backgroundColor: isVerified ? '#f8fafc' : '#ffffff',
                  fontSize: '0.875rem',
                  resize: 'vertical'
                }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: '600', marginBottom: '6px' }}>
                Impression / Conclusion
              </label>
              <textarea
                rows={3}
                value={impression}
                onChange={(e) => setImpression(e.target.value)}
                placeholder="Summary impression and recommended follow-up..."
                disabled={isVerified}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  backgroundColor: isVerified ? '#f8fafc' : '#ffffff',
                  fontSize: '0.875rem',
                  resize: 'vertical'
                }}
              />
            </div>

            {!isVerified ? (
              <div style={{ display: 'flex', gap: '12px', marginTop: '12px', justifyContent: 'flex-end' }}>
                <button
                  type="submit"
                  disabled={reportActionLoading}
                  className="btn-secondary"
                >
                  Save Draft (REPORTED)
                </button>
                <button
                  type="button"
                  onClick={handleVerifyReport}
                  disabled={reportActionLoading}
                  className="btn-primary"
                  style={{ backgroundColor: '#15803d' }}
                >
                  <Lock size={15} />
                  <span>Verify &amp; Finalize</span>
                </button>
              </div>
            ) : null}
          </form>
        </div>

      </div>
    </div>
  );
}
