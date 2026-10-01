package com.dxr.health.service;

import com.dxr.health.ai.TriageModelAdapter;
import com.dxr.health.dto.TriageResponse;
import com.dxr.health.exception.ResourceNotFoundException;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.AuditEventRepository;
import com.dxr.health.repository.ImageAssetRepository;
import com.dxr.health.repository.StudyRepository;
import com.dxr.health.repository.TriageResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TriageService {

    private static final Logger log = LoggerFactory.getLogger(TriageService.class);

    private final StudyRepository studyRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final TriageResultRepository triageResultRepository;
    private final AuditEventRepository auditEventRepository;
    private final TriageModelAdapter triageModelAdapter;

    public TriageService(
            StudyRepository studyRepository,
            ImageAssetRepository imageAssetRepository,
            TriageResultRepository triageResultRepository,
            AuditEventRepository auditEventRepository,
            TriageModelAdapter triageModelAdapter
    ) {
        this.studyRepository = studyRepository;
        this.imageAssetRepository = imageAssetRepository;
        this.triageResultRepository = triageResultRepository;
        this.auditEventRepository = auditEventRepository;
        this.triageModelAdapter = triageModelAdapter;
    }

    @Transactional
    public TriageResponse runTriage(String studyId) {
        ImagingStudy study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Imaging study not found: " + studyId));

        if (study.getWorkflowStatus() == WorkflowStatus.VERIFIED) {
            throw new WorkflowConflictException("Cannot triage a study that has already been verified and locked: " + studyId);
        }

        List<ImageAsset> assets = imageAssetRepository.findByStudyId(studyId);
        if (assets.isEmpty()) {
            throw new WorkflowConflictException("Cannot run AI triage: Study " + studyId + " has no associated image assets.");
        }

        ImageAsset primaryAsset = assets.getFirst();

        TriageResult result;
        try {
            result = triageModelAdapter.performTriage(study, primaryAsset);
            result.setStatus(TriageStatus.READY);

            // Transition study status to TRIAGED if it was in VALIDATED
            if (study.getWorkflowStatus() == WorkflowStatus.VALIDATED || study.getWorkflowStatus() == WorkflowStatus.RECEIVED) {
                study.setWorkflowStatus(WorkflowStatus.TRIAGED);
                studyRepository.save(study);
            }

            auditEventRepository.save(new AuditEvent(
                    studyId,
                    "TRIAGE_COMPLETED",
                    "AI_ORCHESTRATOR",
                    "Assigned priority " + result.getPriority() + " with confidence " + result.getConfidence() + " (" + result.getModelName() + " v" + result.getModelVersion() + ")"
            ));
            log.info("AI triage completed successfully for study {}: priority={}", studyId, result.getPriority());

        } catch (Exception ex) {
            log.error("AI triage adapter failed for study {}: {}. Activating safe chronological fallback.", studyId, ex.getMessage());

            // Safe fallback rule: Study remains reviewable; persist FAILED triage status with ROUTINE priority
            result = new TriageResult(studyId, TriagePriority.ROUTINE, 0.0, triageModelAdapter.getAdapterName(), triageModelAdapter.getModelVersion());
            result.setStatus(TriageStatus.FAILED);
            result.setCandidateLabelsList(List.of("AI Evaluation Failed - Safe Fallback"));
            result.setProcessingTimeMs(0L);

            auditEventRepository.save(new AuditEvent(
                    studyId,
                    "TRIAGE_FAILED",
                    "AI_ORCHESTRATOR",
                    "Triage adapter error: " + ex.getMessage() + ". Preserving study for manual review."
            ));
        }

        TriageResult savedResult = triageResultRepository.save(result);

        return new TriageResponse(
                savedResult.getTriageId(),
                savedResult.getStudyId(),
                savedResult.getPriority(),
                savedResult.getConfidence(),
                savedResult.getCandidateLabelsList(),
                savedResult.getModelName(),
                savedResult.getModelVersion(),
                savedResult.getStatus(),
                savedResult.getProcessingTimeMs(),
                savedResult.getExplanationRef(),
                savedResult.getCreatedAt()
        );
    }

    public TriageResponse getTriageResult(String studyId) {
        TriageResult result = triageResultRepository.findTopByStudyIdOrderByCreatedAtDesc(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("No triage result found for study: " + studyId));

        return new TriageResponse(
                result.getTriageId(),
                result.getStudyId(),
                result.getPriority(),
                result.getConfidence(),
                result.getCandidateLabelsList(),
                result.getModelName(),
                result.getModelVersion(),
                result.getStatus(),
                result.getProcessingTimeMs(),
                result.getExplanationRef(),
                result.getCreatedAt()
        );
    }
}
