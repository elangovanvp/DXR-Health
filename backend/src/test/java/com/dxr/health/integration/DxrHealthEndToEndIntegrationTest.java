package com.dxr.health.integration;

import com.dxr.health.dto.*;
import com.dxr.health.model.*;
import com.dxr.health.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DxrHealthEndToEndIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private WorklistService worklistService;

    @Autowired
    private ReportService reportService;

    @Test
    @DisplayName("fullWorkflow_withSyntheticStudy_completesSuccessfully - End-to-end integration: Patient -> Study -> Ingest -> Triage -> Worklist -> Report -> Verify")
    void fullWorkflow_withSyntheticStudy_completesSuccessfully() {
        // 1. Create Patient
        CreatePatientRequest patReq = new CreatePatientRequest("SYNTH-E2E-PATIENT-01", "55-64", "M");
        Patient patient = patientService.createPatient(patReq);
        assertThat(patient).isNotNull();
        assertThat(patient.getPatientId()).startsWith("PAT-");

        // 2. Create Study
        CreateStudyRequest studyReq = new CreateStudyRequest(
                patient.getPatientId(),
                "CHEST",
                "DX",
                "Acute tension pneumothorax suspicion"
        );
        ImagingStudy study = studyService.createStudy(studyReq);
        assertThat(study).isNotNull();
        assertThat(study.getWorkflowStatus()).isEqualTo(WorkflowStatus.RECEIVED);

        // 3. Upload Image Asset (DICOM with preamble)
        byte[] syntheticDicom = new byte[256];
        syntheticDicom[128] = 'D';
        syntheticDicom[129] = 'I';
        syntheticDicom[130] = 'C';
        syntheticDicom[131] = 'M';
        MockMultipartFile dicomFile = new MockMultipartFile(
                "file",
                "chest_study.dcm",
                "application/dicom",
                syntheticDicom
        );

        UploadResponse uploadRes = ingestionService.ingestImage(study.getStudyId(), dicomFile);
        assertThat(uploadRes).isNotNull();
        assertThat(uploadRes.workflowStatus()).isEqualTo(WorkflowStatus.VALIDATED.name());

        // 4. Run AI Triage
        TriageResponse triageRes = triageService.runTriage(study.getStudyId());
        assertThat(triageRes).isNotNull();
        assertThat(triageRes.priority()).isEqualTo(TriagePriority.CRITICAL);
        assertThat(triageRes.status()).isEqualTo(TriageStatus.READY);

        // 5. Query Prioritized Worklist
        List<WorklistItemResponse> worklist = worklistService.getPrioritizedWorklist(null);
        assertThat(worklist).isNotEmpty();
        WorklistItemResponse foundItem = worklist.stream()
                .filter(item -> item.studyId().equals(study.getStudyId()))
                .findFirst()
                .orElse(null);
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.priority()).isEqualTo(TriagePriority.CRITICAL);

        // 6. Transition to IN_REVIEW & Save Draft Report
        studyService.updateWorkflowStatus(study.getStudyId(), new UpdateStatusRequest(WorkflowStatus.IN_REVIEW, "Dr. Sarah", "Claimed review"));

        ReportRequest draftReq = new ReportRequest(
                "Large right-sided tension pneumothorax identified with mediastinal shift.",
                "Critical tension pneumothorax. Immediate thoracostomy decompression indicated.",
                "Dr. Sarah Chen, MD",
                false
        );
        DiagnosticReport draftReport = reportService.submitOrUpdateReport(study.getStudyId(), draftReq);
        assertThat(draftReport.getReportStatus()).isEqualTo(ReportStatus.DRAFT);

        // 7. Verify & Finalize Report
        ReportRequest finalReq = new ReportRequest(
                "Large right-sided tension pneumothorax identified with mediastinal shift. Chest tube in place post-reduction.",
                "Critical tension pneumothorax stabilized.",
                "Dr. Sarah Chen, MD",
                true
        );
        DiagnosticReport finalizedReport = reportService.submitOrUpdateReport(study.getStudyId(), finalReq);
        assertThat(finalizedReport.getReportStatus()).isEqualTo(ReportStatus.FINAL);
        assertThat(finalizedReport.getVerifiedAt()).isNotNull();

        // 8. Verify study is in VERIFIED status and excluded from unread worklist
        StudyDetailResponse studyDetail = studyService.getStudyDetails(study.getStudyId());
        assertThat(studyDetail.workflowStatus()).isEqualTo(WorkflowStatus.VERIFIED);
        assertThat(studyDetail.images()).hasSize(1);
        assertThat(studyDetail.triageResult()).isNotNull();
        assertThat(studyDetail.report()).isNotNull();
        assertThat(studyDetail.auditEvents()).isNotEmpty();

        List<WorklistItemResponse> updatedWorklist = worklistService.getPrioritizedWorklist(null);
        boolean stillInUnreadQueue = updatedWorklist.stream()
                .anyMatch(item -> item.studyId().equals(study.getStudyId()));
        assertThat(stillInUnreadQueue).isFalse();
    }
}
