package com.edu.lms.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Data
public class StorageProperties {

    /** null/blank in prod → SDK talks to real AWS endpoints. Set for MinIO. */
    private String endpoint;

    private String region = "us-east-1";
    private String bucket;

    /** Leave blank in prod — falls back to IAM role via DefaultCredentialsProvider. */
    private String accessKey;
    private String secretKey;

    /** MinIO needs path-style (http://host:9000/bucket/key). AWS S3 uses virtual-hosted style. */
    private boolean pathStyleAccess = false;

    private long presignedUrlExpirySeconds = 10800; // 3 hours
    private long maxVideoSizeBytes = 2147483648L;    // 2GB
}