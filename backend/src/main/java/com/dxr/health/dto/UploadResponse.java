package com.dxr.health.dto;

import java.time.Instant;

public record UploadResponse(
    String imageId,
    String studyId,
    String objectKey,
    String sha256,
    String format,
    Long sizeBytes,
    Integer rows,
    Integer columns,
    String sopInstanceUid,
    Instant acquisitionTime,
    String workflowStatus
) {}
