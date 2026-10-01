package com.dxr.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateStudyRequest(
    @NotBlank(message = "Patient ID is required")
    String patientId,

    @NotBlank(message = "Body region is required")
    @Size(max = 64, message = "Body region cannot exceed 64 characters")
    String bodyRegion,

    @NotBlank(message = "Modality is required")
    @Pattern(regexp = "^(DX|CR)$", message = "Modality must be DX (Digital Radiography) or CR (Computed Radiography)")
    String modality,

    @Size(max = 512, message = "Clinical indication cannot exceed 512 characters")
    String clinicalIndication
) {}
