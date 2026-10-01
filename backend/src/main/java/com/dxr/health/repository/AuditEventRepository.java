package com.dxr.health.repository;

import com.dxr.health.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findByStudyIdOrderByTimestampDesc(String studyId);
}
