package com.dxr.health.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportRequest(
    @NotBlank(message = "Findings are required for report authoring")
    String findings,

    @NotBlank(message = "Impression is required for report authoring")
    String impression,

    @NotBlank(message = "Author reference is required")
    String authorRef,

    boolean verified
) {}
