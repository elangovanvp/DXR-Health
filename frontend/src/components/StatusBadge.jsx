import React from 'react';

const statusConfig = {
  RECEIVED: { label: 'Received', bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' },
  VALIDATED: { label: 'Validated', bg: '#e0f2fe', color: '#0369a1', border: '#bae6fd' },
  TRIAGED: { label: 'Triaged', bg: '#fef3c7', color: '#b45309', border: '#fde68a' },
  IN_REVIEW: { label: 'In Review', bg: '#ede9fe', color: '#6d28d9', border: '#ddd6fe' },
  REPORTED: { label: 'Reported', bg: '#fce7f3', color: '#be185d', border: '#fbcfe8' },
  VERIFIED: { label: 'Verified', bg: '#dcfce7', color: '#15803d', border: '#bbf7d0' }
};

export default function StatusBadge({ status }) {
  const config = statusConfig[status] || {
    label: status || 'UNKNOWN',
    bg: '#f1f5f9',
    color: '#475569',
    border: '#cbd5e1'
  };

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: '3px 9px',
        fontSize: '0.75rem',
        fontWeight: '600',
        borderRadius: '9999px',
        backgroundColor: config.bg,
        color: config.color,
        border: `1px solid ${config.border}`,
        letterSpacing: '0.025em',
        textTransform: 'uppercase'
      }}
    >
      {config.label}
    </span>
  );
}
