package com.dxr.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class ImageMetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(ImageMetadataExtractor.class);

    public record ExtractedMetadata(
            String format,
            Integer rows,
            Integer columns,
            String sopInstanceUid,
            String seriesInstanceUid,
            String studyInstanceUid,
            String detectedModality,
            Instant acquisitionTime
    ) {}

    public ExtractedMetadata extract(byte[] bytes, String originalFilename) {
        String format = detectFormat(bytes, originalFilename);
        Integer rows = null;
        Integer columns = null;
        String sopUid = "1.2.840.10008.5.1.4.1.1.1." + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String seriesUid = "1.2.840.10008.5.1.4.1.1.2." + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String studyUid = "1.2.840.10008.5.1.4.1.1.3." + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String modality = "DX";

        if ("DICOM".equalsIgnoreCase(format)) {
            // Check for DICM signature at offset 128
            if (bytes.length >= 132 &&
                    bytes[128] == 'D' && bytes[129] == 'I' && bytes[130] == 'C' && bytes[131] == 'M') {
                log.info("Detected standard DICM header at byte offset 128");
                rows = extractDicomShort(bytes, 0x0028, 0x0010);
                columns = extractDicomShort(bytes, 0x0028, 0x0011);
                String parsedModality = extractDicomString(bytes, 0x0008, 0x0060);
                if (parsedModality != null && !parsedModality.isBlank()) {
                    modality = parsedModality.trim();
                }
            } else {
                // Simplified DICOM without 128-byte preamble
                rows = 1024;
                columns = 1024;
            }
        } else {
            // JPEG or PNG
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img != null) {
                    rows = img.getHeight();
                    columns = img.getWidth();
                }
            } catch (Exception e) {
                log.warn("Could not read image dimensions using ImageIO: {}", e.getMessage());
            }
        }

        if (rows == null) rows = 1024;
        if (columns == null) columns = 1024;

        return new ExtractedMetadata(
                format,
                rows,
                columns,
                sopUid,
                seriesUid,
                studyUid,
                modality,
                Instant.now()
        );
    }

    private String detectFormat(byte[] bytes, String filename) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".dcm") || lower.endsWith(".ima")) {
                return "DICOM";
            }
            if (lower.endsWith(".png")) {
                return "PNG";
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return "JPEG";
            }
        }
        if (bytes.length >= 132 &&
                bytes[128] == 'D' && bytes[129] == 'I' && bytes[130] == 'C' && bytes[131] == 'M') {
            return "DICOM";
        }
        if (bytes.length >= 8 &&
                (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "PNG";
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
            return "JPEG";
        }
        return "UNKNOWN";
    }

    private Integer extractDicomShort(byte[] bytes, int group, int element) {
        int index = findTag(bytes, group, element);
        if (index != -1 && index + 8 <= bytes.length) {
            // Implicit/explicit VR 2-byte short
            int val = (bytes[index + 6] & 0xFF) | ((bytes[index + 7] & 0xFF) << 8);
            return val > 0 ? val : 1024;
        }
        return null;
    }

    private String extractDicomString(byte[] bytes, int group, int element) {
        int index = findTag(bytes, group, element);
        if (index != -1 && index + 10 <= bytes.length) {
            int len = (bytes[index + 6] & 0xFF) | ((bytes[index + 7] & 0xFF) << 8);
            if (len > 0 && len < 64 && index + 8 + len <= bytes.length) {
                return new String(bytes, index + 8, len, StandardCharsets.US_ASCII).trim();
            }
        }
        return null;
    }

    private int findTag(byte[] bytes, int group, int element) {
        byte g0 = (byte) (group & 0xFF);
        byte g1 = (byte) ((group >> 8) & 0xFF);
        byte e0 = (byte) (element & 0xFF);
        byte e1 = (byte) ((element >> 8) & 0xFF);

        for (int i = 132; i < bytes.length - 8; i++) {
            if (bytes[i] == g0 && bytes[i + 1] == g1 && bytes[i + 2] == e0 && bytes[i + 3] == e1) {
                return i;
            }
        }
        return -1;
    }
}
