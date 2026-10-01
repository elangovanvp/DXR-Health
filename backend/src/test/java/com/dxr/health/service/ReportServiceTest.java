package com.dxr.health.service;

import com.dxr.health.dto.ReportRequest;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.AuditEventRepository;
import com.dxr.health.repository.DiagnosticReportRepository;
import com.dxr.health.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DiagnosticReportRepository reportRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, studyRepository, auditEventRepository);
    }

    @Test
    @DisplayName("verifyReport_whenAlreadyVerified_returns409 - Rejects modification once report is verified and immutable")
    void verifyReport_whenAlreadyVerified_returns409() {
        String studyId = "STU-VERIFIED";
        ImagingStudy study = new ImagingStudy("PAT-1", "CHEST", "DX", "Checkup");
        study.setStudyId(studyId);
        study.setWorkflowStatus(WorkflowStatus.VERIFIED);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));

        ReportRequest request = new ReportRequest(
                "Modified findings",
                "Modified impression",
                "Dr. Smith",
                true
        );

        assertThatThrownBy(() -> reportService.submitOrUpdateReport(studyId, request))
                .isInstanceOf(WorkflowConflictException.class)
                .hasMessageContaining("already VERIFIED and immutable");

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("verifyReport_whenValid_marksVerified - Verifies draft report and locks study status")
    void verifyReport_whenValid_marksVerified() {
        String studyId = "STU-DRAFT";
        ImagingStudy study = new ImagingStudy("PAT-1", "CHEST", "DX", "Pneumonia");
        study.setStudyId(studyId);
        study.setWorkflowStatus(WorkflowStatus.IN_REVIEW);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(reportRepository.findByStudyId(studyId)).thenReturn(Optional.empty());
        when(reportRepository.save(any(DiagnosticReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportRequest request = new ReportRequest(
                "Clear lung fields. No pneumothorax.",
                "Normal chest radiograph.",
                "Dr. Alice Walker, MD",
                true
        );

        DiagnosticReport report = reportService.submitOrUpdateReport(studyId, request);

        assertThat(report).isNotNull();
        assertThat(report.getReportStatus()).isEqualTo(ReportStatus.FINAL);
        assertThat(report.getVerifiedAt()).isNotNull();
        assertThat(study.getWorkflowStatus()).isEqualTo(WorkflowStatus.VERIFIED);

        verify(studyRepository, times(1)).save(study);
        verify(auditEventRepository, times(1)).save(argThat(evt ->
                "REPORT_VERIFIED".equals(evt.getEventType())
        ));
    }
}
