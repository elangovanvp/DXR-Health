package com.dxr.health.controller;

import com.dxr.health.dto.ReportRequest;
import com.dxr.health.model.DiagnosticReport;
import com.dxr.health.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/studies/{studyId}/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<DiagnosticReport> submitReport(
            @PathVariable("studyId") String studyId,
            @Valid @RequestBody ReportRequest request
    ) {
        DiagnosticReport report = reportService.submitOrUpdateReport(studyId, request);
        return ResponseEntity.ok(report);
    }

    @GetMapping
    public ResponseEntity<DiagnosticReport> getReport(@PathVariable("studyId") String studyId) {
        DiagnosticReport report = reportService.getReport(studyId);
        return ResponseEntity.ok(report);
    }
}
