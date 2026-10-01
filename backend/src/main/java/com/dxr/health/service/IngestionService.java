package com.dxr.health.service;

import com.dxr.health.dto.UploadResponse;
import com.dxr.health.exception.ResourceNotFoundException;
import com.dxr.health.exception.ValidationException;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.AuditEventRepository;
import com.dxr.health.repository.ImageAssetRepository;
import com.dxr.health.repository.StudyRepository;
import com.dxr.health.storage.ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private static final Set<String> ALLOWED_FORMATS = Set.of("DICOM", "JPEG", "PNG");
    private static final Set<String> ALLOWED_MODALITIES = Set.of("DX", "CR");

    private final StudyRepository studyRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final AuditEventRepository auditEventRepository;
    private final ObjectStorageService objectStorageService;
    private final ImageMetadataExtractor metadataExtractor;

    public IngestionService(
            StudyRepository studyRepository,
            ImageAssetRepository imageAssetRepository,
            AuditEventRepository auditEventRepository,
            ObjectStorageService objectStorageService,
            ImageMetadataExtractor metadataExtractor
    ) {
        this.studyRepository = studyRepository;
        this.imageAssetRepository = imageAssetRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectStorageService = objectStorageService;
        this.metadataExtractor = metadataExtractor;
    }

    @Transactional
    public UploadResponse ingestImage(String studyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Uploaded file cannot be null or empty");
        }

        ImagingStudy study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Imaging study not found: " + studyId));

        if (!ALLOWED_MODALITIES.contains(study.getModality().toUpperCase())) {
            throw new ValidationException("Unsupported modality: " + study.getModality() + ". Only DX and CR are supported.");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new ValidationException("Failed to read file bytes: " + e.getMessage());
        }

        ImageMetadataExtractor.ExtractedMetadata meta = metadataExtractor.extract(fileBytes, file.getOriginalFilename());
        if (!ALLOWED_FORMATS.contains(meta.format().toUpperCase())) {
            throw new ValidationException("Unsupported file format: " + meta.format() + ". Only DICOM, JPEG, and PNG are allowed.");
        }

        // Calculate SHA-256
        String sha256 = calculateSha256(fileBytes);

        // Check for duplicate hash on the same study
        if (imageAssetRepository.existsByStudyIdAndSha256(studyId, sha256)) {
            throw new WorkflowConflictException("Duplicate image hash detected for study: " + studyId + " (SHA-256: " + sha256 + ")");
        }

        // Store object in object storage
        String objectKey = "studies/" + studyId + "/" + sha256 + "." + meta.format().toLowerCase();
        objectStorageService.store(objectKey, new ByteArrayInputStream(fileBytes), fileBytes.length, file.getContentType());

        // Commit ImageAsset entity
        ImageAsset asset = new ImageAsset(studyId, objectKey, sha256, meta.format(), (long) fileBytes.length);
        asset.setRows(meta.rows());
        asset.setColumns(meta.columns());
        asset.setSopInstanceUid(meta.sopInstanceUid());
        asset.setSeriesInstanceUid(meta.seriesInstanceUid());
        asset.setStudyInstanceUid(meta.studyInstanceUid());
        asset.setAcquisitionTime(meta.acquisitionTime());
        ImageAsset savedAsset = imageAssetRepository.save(asset);

        // Advance study status to VALIDATED
        if (study.getWorkflowStatus() == WorkflowStatus.RECEIVED) {
            study.setWorkflowStatus(WorkflowStatus.VALIDATED);
            studyRepository.save(study);
        }

        // Record Audit Event
        AuditEvent audit = new AuditEvent(
                studyId,
                "IMAGE_INGESTED",
                "TECHNICIAN",
                "Uploaded " + meta.format() + " image asset: " + savedAsset.getImageId() + " (SHA-256: " + sha256 + ")"
        );
        auditEventRepository.save(audit);

        log.info("Successfully ingested image {} for study {}, status moved to {}", savedAsset.getImageId(), studyId, study.getWorkflowStatus());

        return new UploadResponse(
                savedAsset.getImageId(),
                savedAsset.getStudyId(),
                savedAsset.getObjectKey(),
                savedAsset.getSha256(),
                savedAsset.getFormat(),
                savedAsset.getSizeBytes(),
                savedAsset.getRows(),
                savedAsset.getColumns(),
                savedAsset.getSopInstanceUid(),
                savedAsset.getAcquisitionTime(),
                study.getWorkflowStatus().name()
        );
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
