package com.dxr.health.repository;

import com.dxr.health.model.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageAssetRepository extends JpaRepository<ImageAsset, String> {
    List<ImageAsset> findByStudyId(String studyId);
    boolean existsByStudyIdAndSha256(String studyId, String sha256);
    Optional<ImageAsset> findFirstByStudyIdOrderByCreatedAtDesc(String studyId);
}
