package com.dxr.health.service;

import com.dxr.health.ai.TriageModelAdapter;
import com.dxr.health.dto.TriageResponse;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.AuditEventRepository;
import com.dxr.health.repository.ImageAssetRepository;
import com.dxr.health.repository.StudyRepository;
import com.dxr.health.repository.TriageResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private ImageAssetRepository imageAssetRepository;

    @Mock
    private TriageResultRepository triageResultRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private TriageModelAdapter triageModelAdapter;

    private TriageService triageService;

    @BeforeEach
    void setUp() {
        triageService = new TriageService(
                studyRepository,
                imageAssetRepository,
                triageResultRepository,
                auditEventRepository,
                triageModelAdapter
        );
    }

    @Test
    @DisplayName("runTriage_whenNoImage_returns409 - Cannot triage study without at least one valid image asset")
    void runTriage_whenNoImage_returns409() {
        String studyId = "STU-NO-IMG";
        ImagingStudy study = new ImagingStudy("PAT-1", "CHEST", "DX", "Checkup");
        study.setStudyId(studyId);
        study.setWorkflowStatus(WorkflowStatus.RECEIVED);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(imageAssetRepository.findByStudyId(studyId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> triageService.runTriage(studyId))
                .isInstanceOf(WorkflowConflictException.class)
                .hasMessageContaining("has no associated image assets");

        verify(triageModelAdapter, never()).performTriage(any(), any());
    }

    @Test
    @DisplayName("runTriage_whenAdapterFails_marksFailedButStudyRemainsReviewable - Safe fallback preserves study with status FAILED")
    void runTriage_whenAdapterFails_marksFailedButStudyRemainsReviewable() {
        String studyId = "STU-AI-FAIL";
        ImagingStudy study = new ImagingStudy("PAT-1", "CHEST", "DX", "Fever and cough");
        study.setStudyId(studyId);
        study.setWorkflowStatus(WorkflowStatus.VALIDATED);

        ImageAsset asset = new ImageAsset(studyId, "studies/STU/img.png", "sha256", "PNG", 5000L);
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(imageAssetRepository.findByStudyId(studyId)).thenReturn(List.of(asset));
        when(triageModelAdapter.getAdapterName()).thenReturn("dxr-mock");
        when(triageModelAdapter.getModelVersion()).thenReturn("1.0.0");
        when(triageModelAdapter.performTriage(any(), any())).thenThrow(new RuntimeException("Inference model timeout"));

        when(triageResultRepository.save(any(TriageResult.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TriageResponse response = triageService.runTriage(studyId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(TriageStatus.FAILED);
        assertThat(response.priority()).isEqualTo(TriagePriority.ROUTINE);

        // Verify study was NOT deleted or corrupted
        assertThat(study.getWorkflowStatus()).isEqualTo(WorkflowStatus.VALIDATED);

        // Verify fallback audit event was recorded
        verify(auditEventRepository, times(1)).save(argThat(event ->
                "TRIAGE_FAILED".equals(event.getEventType())
        ));
    }

    @Test
    @DisplayName("runTriage_whenSuccessful_marksTriaged - Assigns priority and advances study status to TRIAGED")
    void runTriage_whenSuccessful_marksTriaged() {
        String studyId = "STU-CRIT";
        ImagingStudy study = new ImagingStudy("PAT-1", "CHEST", "DX", "Suspected pneumothorax");
        study.setStudyId(studyId);
        study.setWorkflowStatus(WorkflowStatus.VALIDATED);

        ImageAsset asset = new ImageAsset(studyId, "studies/STU/img.dcm", "sha256", "DICOM", 10000L);
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(imageAssetRepository.findByStudyId(studyId)).thenReturn(List.of(asset));

        TriageResult mockResult = new TriageResult(studyId, TriagePriority.CRITICAL, 0.95, "dxr-mock", "1.0.0");
        mockResult.setCandidateLabelsList(List.of("Pneumothorax"));
        when(triageModelAdapter.performTriage(eq(study), eq(asset))).thenReturn(mockResult);
        when(triageResultRepository.save(any(TriageResult.class))).thenReturn(mockResult);

        // Act
        TriageResponse response = triageService.runTriage(studyId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.priority()).isEqualTo(TriagePriority.CRITICAL);
        assertThat(response.confidence()).isEqualTo(0.95);
        assertThat(study.getWorkflowStatus()).isEqualTo(WorkflowStatus.TRIAGED);

        verify(studyRepository, times(1)).save(study);
        verify(auditEventRepository, times(1)).save(argThat(event ->
                "TRIAGE_COMPLETED".equals(event.getEventType())
        ));
    }
}
