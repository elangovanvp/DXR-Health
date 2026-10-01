import React from 'react';
import { Activity, ListOrdered, UploadCloud, Users, ShieldAlert, Cpu } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab }) {
  const navItems = [
    { id: 'worklist', label: 'Radiologist Worklist', icon: ListOrdered },
    { id: 'ingestion', label: 'Study Ingestion', icon: UploadCloud },
    { id: 'patients', label: 'Patients Directory', icon: Users },
    { id: 'admin', label: 'System Health & Audit', icon: Activity }
  ];

  return (
    <header style={{
      backgroundColor: '#0f172a',
      color: '#ffffff',
      borderBottom: '1px solid #1e293b',
      position: 'sticky',
      top: 0,
      zIndex: 100
    }}>
      <div style={{
        maxWidth: '1300px',
        margin: '0 auto',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '68px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            backgroundColor: '#0284c7',
            padding: '8px',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Cpu size={24} color="#ffffff" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ fontSize: '1.25rem', fontWeight: '800', letterSpacing: '-0.02em', color: '#f8fafc' }}>
                DXR HEALTH
              </span>
              <span style={{
                fontSize: '0.65rem',
                backgroundColor: '#1e293b',
                color: '#38bdf8',
                padding: '2px 8px',
                borderRadius: '4px',
                fontWeight: '700',
                border: '1px solid #334155'
              }}>
                GE 2026 ALIGNED
              </span>
            </div>
            <p style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
              Cloud-Native AI-Ready Diagnostic X-Ray Workflow
            </p>
          </div>
        </div>

        <nav style={{ display: 'flex', gap: '6px' }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '9px 16px',
                  borderRadius: '6px',
                  fontSize: '0.875rem',
                  fontWeight: '600',
                  border: 'none',
                  backgroundColor: isActive ? '#0284c7' : 'transparent',
                  color: isActive ? '#ffffff' : '#94a3b8',
                  transition: 'all 0.15s ease'
                }}
              >
                <Icon size={17} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </div>
    </header>
  );
}
