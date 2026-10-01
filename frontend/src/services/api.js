import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor for standardized error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const errorData = error.response?.data || {
      message: error.message || 'An unknown network error occurred',
      errorCode: 'NETWORK_ERROR'
    };
    return Promise.reject(errorData);
  }
);

export const PatientService = {
  createPatient: (data) => api.post('/patients', data),
  getPatientStudies: (patientId) => api.get(`/patients/${patientId}/studies`),
  listPatients: () => api.get('/patients')
};

export const StudyService = {
  createStudy: (data) => api.post('/studies', data),
  getStudy: (studyId) => api.get(`/studies/${studyId}`),
  uploadImage: (studyId, formData) =>
    api.post(`/studies/${studyId}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  runTriage: (studyId) => api.post(`/studies/${studyId}/triage`),
  updateStatus: (studyId, data) => api.patch(`/studies/${studyId}/status`, data),
  submitReport: (studyId, data) => api.post(`/studies/${studyId}/reports`, data),
  getReport: (studyId) => api.get(`/studies/${studyId}/reports`)
};

export const WorklistService = {
  getWorklist: (params) => api.get('/worklist', { params })
};

export const ObservabilityService = {
  getHealth: () => axios.get('/actuator/health'),
  getAuditEvents: (studyId) => api.get(`/studies/${studyId}/audit`)
};

export default api;
