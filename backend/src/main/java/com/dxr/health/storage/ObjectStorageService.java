package com.dxr.health.storage;

import java.io.InputStream;

public interface ObjectStorageService {
    void store(String objectKey, InputStream inputStream, long size, String contentType);
    byte[] retrieve(String objectKey);
    boolean exists(String objectKey);
    void delete(String objectKey);
}
