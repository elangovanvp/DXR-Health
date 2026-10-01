package com.dxr.health.service;

import com.dxr.health.dto.UploadResponse;
import com.dxr.health.exception.ValidationException;
import com.dxr.health.exception.WorkflowConflictException;
import com.dxr.health.model.*;
import com.dxr.health.repository.AuditEventRepository;
import com.dxr.health.repository.ImageAssetRepository;
import com.dxr.health.repository.StudyRepository;
import com.dxr.health.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private ImageAssetRepository imageAssetRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ImageMetadataExtractor metadataExtractor = new ImageMetadataExtractor();
        ingestionService = new IngestionService(
                studyRepository,
                imageAssetRepository,
                auditEventRepository,
                objectStorageService,
                metadataExtractor
        );
    }

    @Test
    @DisplayName("uploadDicom_whenValid_storesAssetAndReturns201 - Ingests valid DICOM, stores in S3, marks study VALIDATED")
    void uploadDicom_whenValid_storesAssetAndReturns201() {
        // Arrange
        String studyId = "STU-001";
        ImagingStudy study = new ImagingStudy("PAT-001", "CHEST", "DX", "Routine chest evaluation");
        study.setStudyId(studyId);
        study.setWorkflowStatus(WorkflowStatus.RECEIVED);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(imageAssetRepository.existsByStudyIdAndSha256(eq(studyId), any())).thenReturn(false);

        ImageAsset savedAsset = new ImageAsset(studyId, "studies/STU-001/hash.dcm", "dummysha256", "DICOM", 2048L);
        savedAsset.setImageId("IMG-001");
        savedAsset.setRows(1024);
        savedAsset.setColumns(1024);
        savedAsset.setAcquisitionTime(Instant.now());
        when(imageAssetRepository.save(any(ImageAsset.class))).thenReturn(savedAsset);

        // Build synthetic DICOM file buffer with 128 zero-byte preamble + DICM magic
        byte[] dicomBytes = new byte[256];
        dicomBytes[128] = 'D';
        dicomBytes[129] = 'I';
        dicomBytes[130] = 'C';
        dicomBytes[131] = 'M';

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "chest_xray.dcm",
                "application/dicom",
                dicomBytes
        );

        // Act
        UploadResponse response = ingestionService.ingestImage(studyId, file);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.imageId()).isEqualTo("IMG-001");
        assertThat(response.format()).isEqualTo("DICOM");
        assertThat(response.workflowStatus()).isEqualTo(WorkflowStatus.VALIDATED.name());

        verify(objectStorageService, times(1)).store(any(), any(), eq((long) dicomBytes.length), any());
        verify(studyRepository, times(1)).save(study);
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("uploadImage_whenDuplicateHash_returns409 - Rejects duplicate binary upload on the same study")
    void uploadImage_whenDuplicateHash_returns409() {
        // Arrange
        String studyId = "STU-002";
        ImagingStudy study = new ImagingStudy("PAT-001", "CHEST", "DX", "Chest pain");
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(imageAssetRepository.existsByStudyIdAndSha256(eq(studyId), any())).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "chest.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        // Act & Assert
        assertThatThrownBy(() -> ingestionService.ingestImage(studyId, file))
                .isInstanceOf(WorkflowConflictException.class)
                .hasMessageContaining("Duplicate image hash detected");

        verify(objectStorageService, never()).store(any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("uploadImage_whenUnsupportedFormat_returns400 - Rejects invalid file format")
    void uploadImage_whenUnsupportedFormat_returns400() {
        String studyId = "STU-003";
        ImagingStudy study = new ImagingStudy("PAT-001", "CHEST", "DX", "Checkup");
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "not an image".getBytes()
        );

        assertThatThrownBy(() -> ingestionService.ingestImage(studyId, file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unsupported file format");
    }

    @Test
    @DisplayName("uploadImage_whenEmpty_returns400 - Rejects empty file payload")
    void uploadImage_whenEmpty_returns400() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> ingestionService.ingestImage("STU-004", file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("empty");
    }
}
