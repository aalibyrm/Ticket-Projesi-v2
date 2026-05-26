package com.ticketmanagement.file.infrastructure.storage;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(R2StorageProperties.class)
class R2StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
    S3Presigner r2S3Presigner(R2StorageProperties properties) {
        properties.validateEnabledConfig();
        return S3Presigner.builder()
                .endpointOverride(properties.getEndpoint())
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
    S3Client r2S3Client(R2StorageProperties properties) {
        properties.validateEnabledConfig();
        return S3Client.builder()
                .endpointOverride(properties.getEndpoint())
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
    ObjectStoragePort r2ObjectStoragePort(
            S3Presigner presigner,
            S3Client s3Client,
            R2StorageProperties properties,
            Clock clock) {
        return new R2ObjectStorageAdapter(presigner, s3Client, properties, clock);
    }

    private StaticCredentialsProvider credentialsProvider(R2StorageProperties properties) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                properties.getAccessKeyId(),
                properties.getSecretAccessKey()));
    }
}
