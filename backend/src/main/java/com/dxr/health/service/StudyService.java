package com.dxr.health.service;

import com.dxr.health.dto.CreateStudyRequest;
import com.dxr.health.dto.StudyDetailResponse;
import com.dxr.health.dto.UpdateStatusRequest;
import com.dxr.health.exception.ResourceNotFoundException;
import com.dxr.health.exception.ValidationException;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class StudyService {

    private static final Logger log = LoggerFactory.getLogger(StudyService.class);

    private static final Set<String> ALLOWED_MODALITIES = Set.of("DX", "CR");

    private final StudyRepository studyRepository;
    private final PatientRepository patientRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final TriageResultRepository triageResultRepository;
    private final DiagnosticReportRepository reportRepository;
    private final AuditEventRepository auditEventRepository;

    public StudyService(
            StudyRepository studyRepository,
            PatientRepository patientRepository,
            ImageAssetRepository imageAssetRepository,
            TriageResultRepository triageResultRepository,
            DiagnosticReportRepository reportRepository,
            AuditEventRepository auditEventRepository
    ) {
        this.studyRepository = studyRepository;
        this.patientRepository = patientRepository;
        this.imageAssetRepository = imageAssetRepository;
        this.triageResultRepository = triageResultRepository;
        this.reportRepository = reportRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public ImagingStudy createStudy(CreateStudyRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.patientId()));

        String modality = request.modality().toUpperCase();
        if (!ALLOWED_MODALITIES.contains(modality)) {
            throw new ValidationException("Unsupported modality: " + request.modality() + ". Only DX and CR are permitted.");
        }

        ImagingStudy study = new ImagingStudy(
                patient.getPatientId(),
                request.bodyRegion().toUpperCase(),
                modality,
                request.clinicalIndication()
        );
        ImagingStudy saved = studyRepository.save(study);

        auditEventRepository.save(new AuditEvent(
                saved.getStudyId(),
                "STUDY_CREATED",
                "CLIENT_API",
                "Created Diagnostic X-Ray study request (" + modality + " / " + study.getBodyRegion() + ") for patient " + patient.getSyntheticReference()
        ));

        log.info("Created study {} for patient {}", saved.getStudyId(), patient.getPatientId());
        return saved;
    }

    @Transactional(readOnly = true)
    public StudyDetailResponse getStudyDetails(String studyId) {
        ImagingStudy study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Imaging study not found: " + studyId));

        String patientRef = patientRepository.findById(study.getPatientId())
                .map(Patient::getSyntheticReference)
                .orElse("Unknown");

        List<ImageAsset> images = imageAssetRepository.findByStudyId(studyId);
        TriageResult triage = triageResultRepository.findTopByStudyIdOrderByCreatedAtDesc(studyId).orElse(null);
        DiagnosticReport report = reportRepository.findByStudyId(studyId).orElse(null);
        List<AuditEvent> auditEvents = auditEventRepository.findByStudyIdOrderByTimestampDesc(studyId);

        return new StudyDetailResponse(
                study.getStudyId(),
                study.getPatientId(),
                patientRef,
                study.getBodyRegion(),
                study.getModality(),
                study.getClinicalIndication(),
                study.getWorkflowStatus(),
                study.getCreatedAt(),
                study.getUpdatedAt(),
                images,
                triage,
                report,
                auditEvents
        );
    }

    @Transactional
    public ImagingStudy updateWorkflowStatus(String studyId, UpdateStatusRequest request) {
        ImagingStudy study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Imaging study not found: " + studyId));

        WorkflowStatus current = study.getWorkflowStatus();
        WorkflowStatus target = request.targetStatus();

        if (current == target) {
            return study; // idempotent
        }

        if (!current.canTransitionTo(target)) {
            throw new WorkflowConflictException(
                    "Invalid workflow transition from " + current + " to " + target + " for study " + studyId
            );
        }

        study.setWorkflowStatus(target);
        ImagingStudy updated = studyRepository.save(study);

        auditEventRepository.save(new AuditEvent(
                studyId,
                "STATUS_TRANSITION",
                request.actorRef() != null ? request.actorRef() : "RADIOLOGIST",
                "Workflow status moved from " + current + " to " + target + (request.details() != null ? " (" + request.details() + ")" : "")
        ));

        log.info("Transitioned study {} status: {} -> {}", studyId, current, target);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getStudyAuditEvents(String studyId) {
        if (!studyRepository.existsById(studyId)) {
            throw new ResourceNotFoundException("Imaging study not found: " + studyId);
        }
        return auditEventRepository.findByStudyIdOrderByTimestampDesc(studyId);
    }
}
