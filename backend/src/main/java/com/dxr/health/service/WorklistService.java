package com.dxr.health.service;

import com.dxr.health.dto.WorklistItemResponse;
import com.dxr.health.model.*;
import com.dxr.health.repository.PatientRepository;
import com.dxr.health.repository.StudyRepository;
import com.dxr.health.repository.TriageResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WorklistService {

    private final StudyRepository studyRepository;
    private final TriageResultRepository triageResultRepository;
    private final PatientRepository patientRepository;

    public WorklistService(
            StudyRepository studyRepository,
            TriageResultRepository triageResultRepository,
            PatientRepository patientRepository
    ) {
        this.studyRepository = studyRepository;
        this.triageResultRepository = triageResultRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<WorklistItemResponse> getPrioritizedWorklist(TriagePriority filterPriority) {
        List<ImagingStudy> studies;
        if (filterPriority != null) {
            studies = studyRepository.findPrioritizedUnreadStudiesByPriority(WorkflowStatus.VERIFIED, filterPriority);
        } else {
            studies = studyRepository.findPrioritizedUnreadStudies(WorkflowStatus.VERIFIED);
        }

        // Cache patient lookups for efficiency
        Map<String, String> patientRefMap = new HashMap<>();

        List<WorklistItemResponse> worklist = new ArrayList<>();
        for (ImagingStudy study : studies) {
            String patientRef = patientRefMap.computeIfAbsent(study.getPatientId(), id ->
                    patientRepository.findById(id)
                            .map(Patient::getSyntheticReference)
                            .orElse("Unknown")
            );

            Optional<TriageResult> triageOpt = triageResultRepository.findTopByStudyIdOrderByCreatedAtDesc(study.getStudyId());

            TriagePriority priority = triageOpt.map(TriageResult::getPriority).orElse(TriagePriority.ROUTINE);
            Double confidence = triageOpt.map(TriageResult::getConfidence).orElse(null);
            List<String> labels = triageOpt.map(TriageResult::getCandidateLabelsList).orElse(Collections.emptyList());
            String triageStatus = triageOpt.map(t -> t.getStatus().name()).orElse("NOT_RUN");

            worklist.add(new WorklistItemResponse(
                    study.getStudyId(),
                    study.getPatientId(),
                    patientRef,
                    study.getBodyRegion(),
                    study.getModality(),
                    study.getClinicalIndication(),
                    study.getWorkflowStatus(),
                    priority,
                    confidence,
                    labels,
                    triageStatus,
                    study.getCreatedAt()
            ));
        }

        // Ensure definitive ranking in Java to guarantee strict contract: CRITICAL -> HIGH -> ROUTINE -> chronological
        worklist.sort((a, b) -> {
            int rankA = a.priority() != null ? a.priority().getRank() : 4;
            int rankB = b.priority() != null ? b.priority().getRank() : 4;
            if (rankA != rankB) {
                return Integer.compare(rankA, rankB);
            }
            if (a.createdAt() != null && b.createdAt() != null) {
                return a.createdAt().compareTo(b.createdAt());
            }
            return 0;
        });

        return worklist;
    }
}
