package com.dxr.health.service;

import com.dxr.health.dto.WorklistItemResponse;
import com.dxr.health.model.*;
import com.dxr.health.repository.PatientRepository;
import com.dxr.health.repository.StudyRepository;
import com.dxr.health.repository.TriageResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorklistServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private TriageResultRepository triageResultRepository;

    @Mock
    private PatientRepository patientRepository;

    private WorklistService worklistService;

    @BeforeEach
    void setUp() {
        worklistService = new WorklistService(studyRepository, triageResultRepository, patientRepository);
    }

    @Test
    @DisplayName("worklist_ordersCriticalBeforeHighAndRoutine - Orders CRITICAL before HIGH before ROUTINE, then chronological")
    void worklist_ordersCriticalBeforeHighAndRoutine() {
        // Study 1: ROUTINE created 2 hours ago
        ImagingStudy studyRoutine = new ImagingStudy("PAT-1", "CHEST", "DX", "Routine checkup");
        studyRoutine.setStudyId("STU-ROUTINE");
        studyRoutine.setCreatedAt(Instant.now().minus(2, ChronoUnit.HOURS));

        // Study 2: CRITICAL created 10 minutes ago
        ImagingStudy studyCritical = new ImagingStudy("PAT-2", "CHEST", "DX", "Acute pneumothorax");
        studyCritical.setStudyId("STU-CRITICAL");
        studyCritical.setCreatedAt(Instant.now().minus(10, ChronoUnit.MINUTES));

        // Study 3: HIGH created 30 minutes ago
        ImagingStudy studyHigh = new ImagingStudy("PAT-3", "CHEST", "DX", "Consolidation");
        studyHigh.setStudyId("STU-HIGH");
        studyHigh.setCreatedAt(Instant.now().minus(30, ChronoUnit.MINUTES));

        when(studyRepository.findPrioritizedUnreadStudies(WorkflowStatus.VERIFIED))
                .thenReturn(List.of(studyRoutine, studyCritical, studyHigh));

        TriageResult trRoutine = new TriageResult("STU-ROUTINE", TriagePriority.ROUTINE, 0.70, "mock", "1.0");
        TriageResult trCritical = new TriageResult("STU-CRITICAL", TriagePriority.CRITICAL, 0.95, "mock", "1.0");
        TriageResult trHigh = new TriageResult("STU-HIGH", TriagePriority.HIGH, 0.85, "mock", "1.0");

        when(triageResultRepository.findTopByStudyIdOrderByCreatedAtDesc("STU-ROUTINE")).thenReturn(Optional.of(trRoutine));
        when(triageResultRepository.findTopByStudyIdOrderByCreatedAtDesc("STU-CRITICAL")).thenReturn(Optional.of(trCritical));
        when(triageResultRepository.findTopByStudyIdOrderByCreatedAtDesc("STU-HIGH")).thenReturn(Optional.of(trHigh));

        when(patientRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        List<WorklistItemResponse> worklist = worklistService.getPrioritizedWorklist(null);

        // Assert
        assertThat(worklist).hasSize(3);
        assertThat(worklist.get(0).studyId()).isEqualTo("STU-CRITICAL");
        assertThat(worklist.get(0).priority()).isEqualTo(TriagePriority.CRITICAL);

        assertThat(worklist.get(1).studyId()).isEqualTo("STU-HIGH");
        assertThat(worklist.get(1).priority()).isEqualTo(TriagePriority.HIGH);

        assertThat(worklist.get(2).studyId()).isEqualTo("STU-ROUTINE");
        assertThat(worklist.get(2).priority()).isEqualTo(TriagePriority.ROUTINE);
    }
}
