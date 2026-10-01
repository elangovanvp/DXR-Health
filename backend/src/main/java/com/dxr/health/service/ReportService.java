package com.dxr.health.service;

import com.dxr.health.dto.ReportRequest;
import com.dxr.health.exception.ResourceNotFoundException;
import com.dxr.health.exception.ValidationException;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.AuditEventRepository;
import com.dxr.health.repository.DiagnosticReportRepository;
import com.dxr.health.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final DiagnosticReportRepository reportRepository;
    private final StudyRepository studyRepository;
    private final AuditEventRepository auditEventRepository;

    public ReportService(
            DiagnosticReportRepository reportRepository,
            StudyRepository studyRepository,
            AuditEventRepository auditEventRepository
    ) {
        this.reportRepository = reportRepository;
        this.studyRepository = studyRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public DiagnosticReport submitOrUpdateReport(String studyId, ReportRequest request) {
        ImagingStudy study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Imaging study not found: " + studyId));

        if (study.getWorkflowStatus() == WorkflowStatus.VERIFIED) {
            throw new WorkflowConflictException("Report is locked: Study " + studyId + " is already VERIFIED and immutable.");
        }

        if (request.findings() == null || request.findings().isBlank()) {
            throw new ValidationException("Report findings cannot be blank");
        }
        if (request.impression() == null || request.impression().isBlank()) {
            throw new ValidationException("Report impression cannot be blank");
        }

        Optional<DiagnosticReport> existingOpt = reportRepository.findByStudyId(studyId);
        DiagnosticReport report;

        if (existingOpt.isPresent()) {
            report = existingOpt.get();
            if (report.getReportStatus() == ReportStatus.FINAL) {
                throw new WorkflowConflictException("Cannot modify report: Report is already marked FINAL.");
            }
            report.setFindings(request.findings());
            report.setImpression(request.impression());
            report.setAuthorRef(request.authorRef());
        } else {
            report = new DiagnosticReport(
                    studyId,
                    request.findings(),
                    request.impression(),
                    request.authorRef(),
                    false
            );
        }

        if (request.verified()) {
            report.setReportStatus(ReportStatus.FINAL);
            report.setVerifiedAt(Instant.now());
            study.setWorkflowStatus(WorkflowStatus.VERIFIED);
            studyRepository.save(study);

            auditEventRepository.save(new AuditEvent(
                    studyId,
                    "REPORT_VERIFIED",
                    request.authorRef(),
                    "Final diagnostic report verified and signed into permanent medical record"
            ));
            log.info("Diagnostic report verified for study {} by {}", studyId, request.authorRef());
        } else {
            report.setReportStatus(ReportStatus.DRAFT);
            if (study.getWorkflowStatus() != WorkflowStatus.REPORTED && study.getWorkflowStatus() != WorkflowStatus.VERIFIED) {
                study.setWorkflowStatus(WorkflowStatus.REPORTED);
                studyRepository.save(study);
            }

            auditEventRepository.save(new AuditEvent(
                    studyId,
                    "REPORT_DRAFTED",
                    request.authorRef(),
                    "Draft diagnostic report authored by " + request.authorRef()
            ));
            log.info("Diagnostic report draft saved for study {}", studyId);
        }

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public DiagnosticReport getReport(String studyId) {
        if (!studyRepository.existsById(studyId)) {
            throw new ResourceNotFoundException("Imaging study not found: " + studyId);
        }
        return reportRepository.findByStudyId(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("No diagnostic report found for study: " + studyId));
    }
}
