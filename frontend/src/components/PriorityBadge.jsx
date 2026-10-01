import React from 'react';

const priorityConfig = {
  CRITICAL: { label: 'CRITICAL', bg: '#fee2e2', color: '#b91c1c', border: '#fca5a5' },
  HIGH: { label: 'HIGH', bg: '#ffedd5', color: '#c2410c', border: '#fdba74' },
  ROUTINE: { label: 'ROUTINE', bg: '#d1fae5', color: '#047857', border: '#6ee7b7' },
  PENDING: { label: 'PENDING', bg: '#f1f5f9', color: '#64748b', border: '#cbd5e1' },
  FAILED: { label: 'FAILED', bg: '#fef2f2', color: '#991b1b', border: '#fecaca' }
};

export default function PriorityBadge({ priority }) {
  const config = priorityConfig[priority] || priorityConfig.PENDING;

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: '3px 10px',
        fontSize: '0.75rem',
        fontWeight: '700',
        borderRadius: '6px',
        backgroundColor: config.bg,
        color: config.color,
        border: `1px solid ${config.border}`,
        letterSpacing: '0.05em'
      }}
    >
      {config.label}
    </span>
  );
}
