package com.edu.lms.storage.config;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageProperties props;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccess())
                        .build());

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccess())
                        .build());

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }
        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (props.getAccessKey() != null && !props.getAccessKey().isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
        }
        // Prod on real AWS: no static keys in config at all. The EC2/ECS
        // instance's IAM role supplies temporary rotating credentials automatically.
        return DefaultCredentialsProvider.create();
    }
}
