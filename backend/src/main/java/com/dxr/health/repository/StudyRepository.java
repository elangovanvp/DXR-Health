package com.dxr.health.repository;

import com.dxr.health.model.ImagingStudy;
import com.dxr.health.model.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<ImagingStudy, String> {

    List<ImagingStudy> findByPatientIdOrderByCreatedAtDesc(String patientId);

    List<ImagingStudy> findByWorkflowStatusNotOrderByCreatedAtAsc(WorkflowStatus excludedStatus);

    @Query("""
        SELECT s FROM ImagingStudy s
        LEFT JOIN TriageResult t ON t.studyId = s.studyId AND t.createdAt = (
            SELECT MAX(tr.createdAt) FROM TriageResult tr WHERE tr.studyId = s.studyId
        )
        WHERE s.workflowStatus != :excludedStatus
        ORDER BY
            CASE
                WHEN t.priority = 'CRITICAL' THEN 1
                WHEN t.priority = 'HIGH' THEN 2
                WHEN t.priority = 'ROUTINE' THEN 3
                ELSE 4
            END ASC,
            s.createdAt ASC
    """)
    List<ImagingStudy> findPrioritizedUnreadStudies(@Param("excludedStatus") WorkflowStatus excludedStatus);

    @Query("""
        SELECT s FROM ImagingStudy s
        JOIN TriageResult t ON t.studyId = s.studyId AND t.createdAt = (
            SELECT MAX(tr.createdAt) FROM TriageResult tr WHERE tr.studyId = s.studyId
        )
        WHERE s.workflowStatus != :excludedStatus
          AND t.priority = :priority
        ORDER BY s.createdAt ASC
    """)
    List<ImagingStudy> findPrioritizedUnreadStudiesByPriority(
            @Param("excludedStatus") WorkflowStatus excludedStatus,
            @Param("priority") com.dxr.health.model.TriagePriority priority);
}
