package com.dxr.health.storage;

import com.dxr.health.exception.StorageException;
import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MinioObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStorageService.class);

    private final String bucketName;
    private final boolean localFallback;
    private final Path localDirectory;
    private MinioClient minioClient;
    private boolean minioAvailable = false;

    public MinioObjectStorageService(
            @Value("${storage.s3.endpoint:http://localhost:9000}") String endpoint,
            @Value("${storage.s3.access-key:dxr_minio_admin}") String accessKey,
            @Value("${storage.s3.secret-key:dxr_minio_secret_key_2026}") String secretKey,
            @Value("${storage.s3.bucket:dxr-xray-images}") String bucketName,
            @Value("${storage.s3.local-fallback:true}") boolean localFallback,
            @Value("${storage.s3.local-directory:./storage-uploads}") String localDir
    ) {
        this.bucketName = bucketName;
        this.localFallback = localFallback;
        this.localDirectory = Paths.get(localDir);

        try {
            Files.createDirectories(this.localDirectory);
        } catch (Exception e) {
            log.warn("Could not create local storage fallback directory: {}", e.getMessage());
        }

        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            }
            this.minioAvailable = true;
            log.info("Successfully connected to MinIO object storage at {}", endpoint);
        } catch (Exception e) {
            log.warn("MinIO server unavailable ({}). Operating with local filesystem storage fallback.", e.getMessage());
            this.minioAvailable = false;
        }
    }

    @Override
    public void store(String objectKey, InputStream inputStream, long size, String contentType) {
        if (minioAvailable && minioClient != null) {
            try {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectKey)
                                .stream(inputStream, size, -1)
                                .contentType(contentType != null ? contentType : "application/octet-stream")
                                .build()
                );
                log.info("Stored object '{}' in MinIO bucket '{}'", objectKey, bucketName);
                return;
            } catch (Exception e) {
                log.warn("MinIO putObject failed for key '{}': {}. Falling back to local storage.", objectKey, e.getMessage());
            }
        }

        if (localFallback) {
            try {
                Path targetPath = localDirectory.resolve(objectKey.replace("/", File.separator));
                Files.createDirectories(targetPath.getParent());
                try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                    inputStream.transferTo(fos);
                }
                log.info("Stored object '{}' locally at {}", objectKey, targetPath.toAbsolutePath());
            } catch (Exception e) {
                throw new StorageException("Failed to store object locally: " + e.getMessage(), e);
            }
        } else {
            throw new StorageException("Object storage unavailable and local fallback is disabled");
        }
    }

    @Override
    public byte[] retrieve(String objectKey) {
        if (minioAvailable && minioClient != null) {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(objectKey).build())) {
                return stream.readAllBytes();
            } catch (Exception e) {
                log.warn("MinIO getObject failed for key '{}': {}", objectKey, e.getMessage());
            }
        }

        if (localFallback) {
            try {
                Path targetPath = localDirectory.resolve(objectKey.replace("/", File.separator));
                if (Files.exists(targetPath)) {
                    return Files.readAllBytes(targetPath);
                }
                throw new StorageException("Object not found in local storage: " + objectKey);
            } catch (Exception e) {
                throw new StorageException("Failed to retrieve object from storage: " + e.getMessage(), e);
            }
        }

        throw new StorageException("Object not found in storage: " + objectKey);
    }

    @Override
    public boolean exists(String objectKey) {
        if (minioAvailable && minioClient != null) {
            try {
                minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectKey).build());
                return true;
            } catch (Exception ignored) {
            }
        }
        Path targetPath = localDirectory.resolve(objectKey.replace("/", File.separator));
        return Files.exists(targetPath);
    }

    @Override
    public void delete(String objectKey) {
        if (minioAvailable && minioClient != null) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectKey).build());
            } catch (Exception ignored) {
            }
        }
        try {
            Path targetPath = localDirectory.resolve(objectKey.replace("/", File.separator));
            Files.deleteIfExists(targetPath);
        } catch (Exception ignored) {
        }
    }
}
