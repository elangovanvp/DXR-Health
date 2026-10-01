package com.dxr.health.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(name = "event_id", length = 64, nullable = false, updatable = false)
    private String eventId;

    @Column(name = "study_id", length = 64, nullable = false)
    private String studyId;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @Column(name = "actor_ref", length = 128)
    private String actorRef;

    @Column(name = "details", length = 1024)
    private String details;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    public AuditEvent() {
    }

    public AuditEvent(String studyId, String eventType, String actorRef, String details) {
        this.eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.studyId = studyId;
        this.eventType = eventType;
        this.actorRef = actorRef;
        this.details = details;
        this.timestamp = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.eventId == null) {
            this.eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getActorRef() {
        return actorRef;
    }

    public void setActorRef(String actorRef) {
        this.actorRef = actorRef;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
