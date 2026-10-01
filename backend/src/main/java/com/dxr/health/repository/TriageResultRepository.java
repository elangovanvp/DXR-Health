package com.dxr.health.repository;

import com.dxr.health.model.TriagePriority;
import com.dxr.health.model.TriageResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TriageResultRepository extends JpaRepository<TriageResult, String> {
    Optional<TriageResult> findTopByStudyIdOrderByCreatedAtDesc(String studyId);
    List<TriageResult> findByStudyIdOrderByCreatedAtDesc(String studyId);
    List<TriageResult> findByPriority(TriagePriority priority);
}
