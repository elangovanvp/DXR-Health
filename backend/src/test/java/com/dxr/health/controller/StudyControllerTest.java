package com.dxr.health.controller;

import com.dxr.health.dto.CreateStudyRequest;
import com.dxr.health.dto.StudyDetailResponse;
import com.dxr.health.exception.GlobalExceptionHandler;
import com.dxr.health.exception.ResourceNotFoundException;
import com.dxr.health.model.*;
import com.dxr.health.service.StudyService;
import com.dxr.health.service.TriageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudyService studyService;

    @Mock
    private TriageService triageService;

    @InjectMocks
    private StudyController studyController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("createStudy_whenPatientMissing_returns404 - Returns 404 when requested patient ID does not exist")
    void createStudy_whenPatientMissing_returns404() throws Exception {
        CreateStudyRequest request = new CreateStudyRequest(
                "NON-EXISTENT-PAT",
                "CHEST",
                "DX",
                "Shortness of breath"
        );

        when(studyService.createStudy(any())).thenThrow(
                new ResourceNotFoundException("Patient not found: NON-EXISTENT-PAT")
        );

        mockMvc.perform(post("/api/v1/studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found: NON-EXISTENT-PAT"));
    }

    @Test
    @DisplayName("getStudy_returnsTriageAndImageStatus - Returns study detail with image assets and triage status")
    void getStudy_returnsTriageAndImageStatus() throws Exception {
        String studyId = "STU-999";
        TriageResult triage = new TriageResult(studyId, TriagePriority.HIGH, 0.88, "dxr-triage-mock", "1.0.0");
        triage.setCandidateLabelsList(List.of("Consolidation"));

        ImageAsset asset = new ImageAsset(studyId, "studies/STU-999/asset.dcm", "sha256abc", "DICOM", 204800L);

        StudyDetailResponse detail = new StudyDetailResponse(
                studyId,
                "PAT-123",
                "SYNTH-PAT-001",
                "CHEST",
                "DX",
                "Productive cough",
                WorkflowStatus.TRIAGED,
                Instant.now(),
                Instant.now(),
                List.of(asset),
                triage,
                null,
                Collections.emptyList()
        );

        when(studyService.getStudyDetails(studyId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/studies/" + studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(studyId))
                .andExpect(jsonPath("$.workflowStatus").value("TRIAGED"))
                .andExpect(jsonPath("$.triageResult.priority").value("HIGH"))
                .andExpect(jsonPath("$.images[0].format").value("DICOM"));
    }
}
