package com.edu.lms.storage.service;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3CompatibleStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;

    @Override
    public String uploadFile(InputStream inputStream, String key, String contentType, long contentLength) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            return key;
        } catch (S3Exception e) {
            throw new BusinessException("Failed to upload file to storage: " + e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration expiry) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new BusinessException("Failed to delete file from storage: " + e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public boolean objectExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
