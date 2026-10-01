package com.dxr.health.dto;

import com.dxr.health.model.TriagePriority;
import com.dxr.health.model.WorkflowStatus;
import java.time.Instant;
import java.util.List;

public record WorklistItemResponse(
    String studyId,
    String patientId,
    String patientReference,
    String bodyRegion,
    String modality,
    String clinicalIndication,
    WorkflowStatus workflowStatus,
    TriagePriority priority,
    Double confidence,
    List<String> candidateLabels,
    String triageStatus,
    Instant createdAt
) {}
