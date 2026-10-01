package com.dxr.health.controller;

import com.dxr.health.dto.CreateStudyRequest;
import com.dxr.health.dto.StudyDetailResponse;
import com.dxr.health.dto.TriageResponse;
import com.dxr.health.dto.UpdateStatusRequest;
import com.dxr.health.model.AuditEvent;
import com.dxr.health.model.ImagingStudy;
import com.dxr.health.service.StudyService;
import com.dxr.health.service.TriageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/studies")
public class StudyController {

    private final StudyService studyService;
    private final TriageService triageService;

    public StudyController(StudyService studyService, TriageService triageService) {
        this.studyService = studyService;
        this.triageService = triageService;
    }

    @PostMapping
    public ResponseEntity<ImagingStudy> createStudy(@Valid @RequestBody CreateStudyRequest request) {
        ImagingStudy created = studyService.createStudy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<StudyDetailResponse> getStudy(@PathVariable("studyId") String studyId) {
        StudyDetailResponse study = studyService.getStudyDetails(studyId);
        return ResponseEntity.ok(study);
    }

    @PatchMapping("/{studyId}/status")
    public ResponseEntity<ImagingStudy> updateWorkflowStatus(
            @PathVariable("studyId") String studyId,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        ImagingStudy updated = studyService.updateWorkflowStatus(studyId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{studyId}/triage")
    public ResponseEntity<TriageResponse> triggerTriage(@PathVariable("studyId") String studyId) {
        TriageResponse triage = triageService.runTriage(studyId);
        return ResponseEntity.ok(triage);
    }

    @GetMapping("/{studyId}/triage")
    public ResponseEntity<TriageResponse> getTriage(@PathVariable("studyId") String studyId) {
        TriageResponse triage = triageService.getTriageResult(studyId);
        return ResponseEntity.ok(triage);
    }

    @GetMapping("/{studyId}/audit")
    public ResponseEntity<List<AuditEvent>> getStudyAudit(@PathVariable("studyId") String studyId) {
        List<AuditEvent> events = studyService.getStudyAuditEvents(studyId);
        return ResponseEntity.ok(events);
    }
}
