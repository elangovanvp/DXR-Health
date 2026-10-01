# DXR Health - Frontend Web Client

The DXR Health frontend is a clinical web application built with React 18 and Vite. It provides dedicated interfaces for:
1. **Technicians / Radiographers**: Register synthetic patients, create imaging studies, upload DICOM/JPEG/PNG files, and inspect metadata extraction.
2. **AI Triage & Prioritization**: Trigger model-agnostic AI inference, view candidate findings, confidence, and prioritization signals (`CRITICAL`, `HIGH`, `ROUTINE`).
3. **Radiologist Worklist**: Prioritized unread worklist ordering urgent studies before routine ones with fallback to chronological order.
4. **Diagnostic Reporting**: Step-by-step reporting workflow (`IN_REVIEW` -> `REPORTED` -> `VERIFIED`) with immutable verification rules.
5. **System Observability & Audit**: Live actuator health monitoring, database status, MinIO storage status, and study audit history.

---

## Architecture

- `src/components/`: Reusable clinical components (Navbar, StatusBadge, PriorityBadge, StudyCard, Modal).
- `src/pages/`:
  - `WorklistPage.jsx`: The radiologist priority triage queue with filters and live statistics.
  - `IngestionPage.jsx`: Study creation & multipart image upload with real-time hash and format validation.
  - `StudyDetailPage.jsx`: Study context, DICOM preview, AI triage execution, and diagnostic report authoring/verification.
  - `PatientPage.jsx`: Patient management and study history lookup.
  - `AdminPage.jsx`: Health checks, storage inspection, and audit trail exploration.
- `src/services/api.js`: Centralized Axios HTTP client with unified error handling and request interceptors.

---

## Local Development

### 1. Install Dependencies
```bash
npm install
```

### 2. Start Development Server
```bash
npm run dev
```
The app will be accessible at `http://localhost:3000`.

### 3. Build for Production
```bash
npm run build
```
Production assets will be output to `/dist`.
