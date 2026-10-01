package com.dxr.health.dto;

import com.dxr.health.model.WorkflowStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull(message = "Target workflow status is required")
    WorkflowStatus targetStatus,

    String actorRef,
    String details
) {}
