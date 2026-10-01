package com.dxr.health.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "image_assets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"study_id", "sha256"}, name = "uk_study_sha256")
})
public class ImageAsset {

    @Id
    @Column(name = "image_id", length = 64, nullable = false, updatable = false)
    private String imageId;

    @Column(name = "study_id", length = 64, nullable = false)
    private String studyId;

    @Column(name = "object_key", length = 256, nullable = false)
    private String objectKey;

    @Column(name = "sha256", length = 64, nullable = false)
    private String sha256;

    @Column(name = "format", length = 16, nullable = false)
    private String format;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "image_rows")
    private Integer rows;

    @Column(name = "image_columns")
    private Integer columns;

    @Column(name = "sop_instance_uid", length = 128)
    private String sopInstanceUid;

    @Column(name = "series_instance_uid", length = 128)
    private String seriesInstanceUid;

    @Column(name = "study_instance_uid", length = 128)
    private String studyInstanceUid;

    @Column(name = "acquisition_time")
    private Instant acquisitionTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ImageAsset() {
    }

    public ImageAsset(String studyId, String objectKey, String sha256, String format, Long sizeBytes) {
        this.imageId = "IMG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.studyId = studyId;
        this.objectKey = objectKey;
        this.sha256 = sha256;
        this.format = format;
        this.sizeBytes = sizeBytes;
        this.createdAt = Instant.now();
        this.acquisitionTime = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.imageId == null) {
            this.imageId = "IMG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    public String getSopInstanceUid() {
        return sopInstanceUid;
    }

    public void setSopInstanceUid(String sopInstanceUid) {
        this.sopInstanceUid = sopInstanceUid;
    }

    public String getSeriesInstanceUid() {
        return seriesInstanceUid;
    }

    public void setSeriesInstanceUid(String seriesInstanceUid) {
        this.seriesInstanceUid = seriesInstanceUid;
    }

    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public void setStudyInstanceUid(String studyInstanceUid) {
        this.studyInstanceUid = studyInstanceUid;
    }

    public Instant getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(Instant acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
