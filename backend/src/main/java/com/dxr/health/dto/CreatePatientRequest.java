package com.dxr.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePatientRequest(
    @NotBlank(message = "Synthetic reference identifier is required")
    @Size(max = 128, message = "Synthetic reference cannot exceed 128 characters")
    String syntheticReference,

    @Size(max = 32, message = "Age band cannot exceed 32 characters")
    String ageBand,

    @Size(max = 8, message = "Sex code cannot exceed 8 characters")
    String sexCode
) {}
