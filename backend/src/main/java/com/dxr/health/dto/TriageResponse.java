package com.dxr.health.dto;

import com.dxr.health.model.TriagePriority;
import com.dxr.health.model.TriageStatus;
import java.time.Instant;
import java.util.List;

public record TriageResponse(
    String triageId,
    String studyId,
    TriagePriority priority,
    Double confidence,
    List<String> candidateLabels,
    String modelName,
    String modelVersion,
    TriageStatus status,
    Long processingTimeMs,
    String explanationRef,
    Instant createdAt
) {}
