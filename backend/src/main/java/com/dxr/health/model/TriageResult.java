package com.dxr.health.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "triage_results")
public class TriageResult {

    @Id
    @Column(name = "triage_id", length = 64, nullable = false, updatable = false)
    private String triageId;

    @Column(name = "study_id", length = 64, nullable = false)
    private String studyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 32, nullable = false)
    private TriagePriority priority;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "candidate_labels", length = 1024)
    private String candidateLabels;

    @Column(name = "model_name", length = 128, nullable = false)
    private String modelName;

    @Column(name = "model_version", length = 64, nullable = false)
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private TriageStatus status;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "explanation_ref", length = 256)
    private String explanationRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TriageResult() {
    }

    public TriageResult(String studyId, TriagePriority priority, Double confidence, String modelName, String modelVersion) {
        this.triageId = "TRG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.studyId = studyId;
        this.priority = priority;
        this.confidence = confidence;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.status = TriageStatus.READY;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.triageId == null) {
            this.triageId = "TRG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public String getTriageId() {
        return triageId;
    }

    public void setTriageId(String triageId) {
        this.triageId = triageId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<String> getCandidateLabelsList() {
        if (candidateLabels == null || candidateLabels.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(candidateLabels.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public void setCandidateLabelsList(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            this.candidateLabels = null;
        } else {
            this.candidateLabels = String.join(",", labels);
        }
    }

    public String getCandidateLabels() {
        return candidateLabels;
    }

    public void setCandidateLabels(String candidateLabels) {
        this.candidateLabels = candidateLabels;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public TriageStatus getStatus() {
        return status;
    }

    public void setStatus(TriageStatus status) {
        this.status = status;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getExplanationRef() {
        return explanationRef;
    }

    public void setExplanationRef(String explanationRef) {
        this.explanationRef = explanationRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
