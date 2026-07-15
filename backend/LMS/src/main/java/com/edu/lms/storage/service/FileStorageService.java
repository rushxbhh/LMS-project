package com.edu.lms.storage.service;
import java.io.InputStream;
import java.time.Duration;

public interface FileStorageService {
    String uploadFile(InputStream inputStream, String key, String contentType, long contentLength);
    String generatePresignedUrl(String key, Duration expiry);
    void deleteFile(String key);
    boolean objectExists(String key);

}