package com.dxr.health.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    String errorCode,
    String message,
    int status,
    Instant timestamp,
    Map<String, String> details
) {
    public ErrorResponse(String errorCode, String message, int status) {
        this(errorCode, message, status, Instant.now(), null);
    }

    public ErrorResponse(String errorCode, String message, int status, Map<String, String> details) {
        this(errorCode, message, status, Instant.now(), details);
    }
}
