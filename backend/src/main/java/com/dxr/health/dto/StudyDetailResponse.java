package com.dxr.health.dto;

import com.dxr.health.model.*;
import java.time.Instant;
import java.util.List;

public record StudyDetailResponse(
    String studyId,
    String patientId,
    String patientReference,
    String bodyRegion,
    String modality,
    String clinicalIndication,
    WorkflowStatus workflowStatus,
    Instant createdAt,
    Instant updatedAt,
    List<ImageAsset> images,
    TriageResult triageResult,
    DiagnosticReport report,
    List<AuditEvent> auditEvents
) {}
