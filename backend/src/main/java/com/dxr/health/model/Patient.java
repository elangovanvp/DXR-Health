package com.dxr.health.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @Column(name = "patient_id", length = 64, nullable = false, updatable = false)
    private String patientId;

    @Column(name = "synthetic_reference", length = 128, nullable = false, unique = true)
    private String syntheticReference;

    @Column(name = "age_band", length = 32)
    private String ageBand;

    @Column(name = "sex_code", length = 8)
    private String sexCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Patient() {
    }

    public Patient(String syntheticReference, String ageBand, String sexCode) {
        this.patientId = "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.syntheticReference = syntheticReference;
        this.ageBand = ageBand;
        this.sexCode = sexCode;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.patientId == null) {
            this.patientId = "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSyntheticReference() {
        return syntheticReference;
    }

    public void setSyntheticReference(String syntheticReference) {
        this.syntheticReference = syntheticReference;
    }

    public String getAgeBand() {
        return ageBand;
    }

    public void setAgeBand(String ageBand) {
        this.ageBand = ageBand;
    }

    public String getSexCode() {
        return sexCode;
    }

    public void setSexCode(String sexCode) {
        this.sexCode = sexCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
