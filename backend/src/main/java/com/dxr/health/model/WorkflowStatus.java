package com.dxr.health.model;

public enum WorkflowStatus {
    RECEIVED,
    VALIDATED,
    TRIAGED,
    IN_REVIEW,
    REPORTED,
    VERIFIED;

    public boolean canTransitionTo(WorkflowStatus next) {
        if (this == VERIFIED) {
            return false; // Final and immutable
        }
        return switch (this) {
            case RECEIVED -> next == VALIDATED;
            case VALIDATED -> next == TRIAGED || next == IN_REVIEW;
            case TRIAGED -> next == IN_REVIEW;
            case IN_REVIEW -> next == REPORTED;
            case REPORTED -> next == REPORTED || next == VERIFIED;
            case VERIFIED -> false;
        };
    }
}
