package com.dxr.health.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "imaging_studies")
public class ImagingStudy {

    @Id
    @Column(name = "study_id", length = 64, nullable = false, updatable = false)
    private String studyId;

    @Column(name = "patient_id", length = 64, nullable = false)
    private String patientId;

    @Column(name = "body_region", length = 64, nullable = false)
    private String bodyRegion;

    @Column(name = "modality", length = 16, nullable = false)
    private String modality;

    @Column(name = "clinical_indication", length = 512)
    private String clinicalIndication;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", length = 32, nullable = false)
    private WorkflowStatus workflowStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ImagingStudy() {
    }

    public ImagingStudy(String patientId, String bodyRegion, String modality, String clinicalIndication) {
        this.studyId = "STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.patientId = patientId;
        this.bodyRegion = bodyRegion;
        this.modality = modality;
        this.clinicalIndication = clinicalIndication;
        this.workflowStatus = WorkflowStatus.RECEIVED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.studyId == null) {
            this.studyId = "STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.workflowStatus == null) {
            this.workflowStatus = WorkflowStatus.RECEIVED;
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getBodyRegion() {
        return bodyRegion;
    }

    public void setBodyRegion(String bodyRegion) {
        this.bodyRegion = bodyRegion;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getClinicalIndication() {
        return clinicalIndication;
    }

    public void setClinicalIndication(String clinicalIndication) {
        this.clinicalIndication = clinicalIndication;
    }

    public WorkflowStatus getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkflowStatus workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
