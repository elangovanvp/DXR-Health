package com.dxr.health.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "diagnostic_reports")
public class DiagnosticReport {

    @Id
    @Column(name = "report_id", length = 64, nullable = false, updatable = false)
    private String reportId;

    @Column(name = "study_id", length = 64, nullable = false, unique = true)
    private String studyId;

    @Lob
    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Lob
    @Column(name = "impression", columnDefinition = "TEXT")
    private String impression;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", length = 32, nullable = false)
    private ReportStatus reportStatus;

    @Column(name = "author_ref", length = 128, nullable = false)
    private String authorRef;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public DiagnosticReport() {
    }

    public DiagnosticReport(String studyId, String findings, String impression, String authorRef, boolean verified) {
        this.reportId = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.studyId = studyId;
        this.findings = findings;
        this.impression = impression;
        this.authorRef = authorRef;
        this.reportStatus = verified ? ReportStatus.FINAL : ReportStatus.DRAFT;
        this.verifiedAt = verified ? Instant.now() : null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.reportId == null) {
            this.reportId = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.reportStatus == null) {
            this.reportStatus = ReportStatus.DRAFT;
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

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

    public ReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getAuthorRef() {
        return authorRef;
    }

    public void setAuthorRef(String authorRef) {
        this.authorRef = authorRef;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
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
