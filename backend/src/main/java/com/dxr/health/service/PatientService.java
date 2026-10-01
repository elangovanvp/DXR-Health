package com.dxr.health.service;

import com.dxr.health.dto.CreatePatientRequest;
import com.dxr.health.exception.ResourceNotFoundException;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.ImagingStudy;
import com.dxr.health.model.Patient;
import com.dxr.health.repository.PatientRepository;
import com.dxr.health.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final StudyRepository studyRepository;

    public PatientService(PatientRepository patientRepository, StudyRepository studyRepository) {
        this.patientRepository = patientRepository;
        this.studyRepository = studyRepository;
    }

    @Transactional
    public Patient createPatient(CreatePatientRequest request) {
        if (patientRepository.existsBySyntheticReference(request.syntheticReference())) {
            throw new WorkflowConflictException("A synthetic patient with reference '" + request.syntheticReference() + "' already exists.");
        }
        Patient patient = new Patient(
                request.syntheticReference(),
                request.ageBand(),
                request.sexCode()
        );
        return patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public Patient getPatient(String patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
    }

    @Transactional(readOnly = true)
    public List<Patient> listAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ImagingStudy> getPatientStudies(String patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return studyRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }
}
