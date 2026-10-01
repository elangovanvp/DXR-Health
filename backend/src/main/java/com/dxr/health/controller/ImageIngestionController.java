package com.dxr.health.controller;

import com.dxr.health.dto.UploadResponse;
import com.dxr.health.service.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/studies/{studyId}/images")
public class ImageIngestionController {

    private final IngestionService ingestionService;

    public ImageIngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImage(
            @PathVariable("studyId") String studyId,
            @RequestParam("file") MultipartFile file
    ) {
        UploadResponse response = ingestionService.ingestImage(studyId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
