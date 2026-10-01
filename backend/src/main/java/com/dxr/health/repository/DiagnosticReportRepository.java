package com.dxr.health.repository;

import com.dxr.health.model.DiagnosticReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticReportRepository extends JpaRepository<DiagnosticReport, String> {
    Optional<DiagnosticReport> findByStudyId(String studyId);
    boolean existsByStudyId(String studyId);
}
