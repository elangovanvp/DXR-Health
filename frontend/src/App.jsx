import React, { useState } from 'react';
import Navbar from './components/Navbar';
import WorklistPage from './pages/WorklistPage';
import IngestionPage from './pages/IngestionPage';
import PatientPage from './pages/PatientPage';
import AdminPage from './pages/AdminPage';
import StudyDetailPage from './pages/StudyDetailPage';

export default function App() {
  const [activeTab, setActiveTab] = useState('worklist');
  const [selectedStudyId, setSelectedStudyId] = useState(null);

  const navigateToStudy = (studyId) => {
    setSelectedStudyId(studyId);
    setActiveTab('study-detail');
  };

  return (
    <div className="app-container">
      <Navbar
        activeTab={activeTab === 'study-detail' ? 'worklist' : activeTab}
        setActiveTab={(tab) => {
          setSelectedStudyId(null);
          setActiveTab(tab);
        }}
      />

      <main className="main-content">
        {activeTab === 'worklist' && (
          <WorklistPage onSelectStudy={navigateToStudy} />
        )}

        {activeTab === 'ingestion' && (
          <IngestionPage onStudyCreated={navigateToStudy} />
        )}

        {activeTab === 'patients' && (
          <PatientPage onSelectStudy={navigateToStudy} />
        )}

        {activeTab === 'admin' && <AdminPage />}

        {activeTab === 'study-detail' && (
          <StudyDetailPage
            studyId={selectedStudyId}
            onBack={() => setActiveTab('worklist')}
          />
        )}
      </main>

      <footer style={{
        textAlign: 'center',
        padding: '16px',
        fontSize: '0.8rem',
        color: '#64748b',
        borderTop: '1px solid #e2e8f0',
        backgroundColor: '#ffffff'
      }}>
        DXR Health &copy; 2026 &mdash; Educational Prototype for GE HealthCare Precision Care Challenge 2026.
        Synthetic data only. AI triage supports priority ordering; radiologist provides definitive diagnosis.
      </footer>
    </div>
  );
}
