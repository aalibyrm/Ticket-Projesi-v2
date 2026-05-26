package com.ticketmanagement.file.infrastructure.storage;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import com.ticketmanagement.file.application.storage.PresignedObjectOperation;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
class R2ObjectStorageAdapter implements ObjectStoragePort {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final R2StorageProperties properties;
    private final Clock clock;

    @Override
    public PresignedObjectOperation createUploadUrl(String objectKey, String contentType) {
        requireObjectKey(objectKey);
        requireContentType(contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();
        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(properties.getPresignUploadTtl())
                .putObjectRequest(putObjectRequest)
                .build());

        return new PresignedObjectOperation(
                "PUT",
                URI.create(presignedRequest.url().toString()),
                Instant.now(clock).plus(properties.getPresignUploadTtl()),
                Map.of(CONTENT_TYPE_HEADER, contentType));
    }

    @Override
    public PresignedObjectOperation createDownloadUrl(String objectKey) {
        requireObjectKey(objectKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(properties.getPresignDownloadTtl())
                .getObjectRequest(getObjectRequest)
                .build());

        return new PresignedObjectOperation(
                "GET",
                URI.create(presignedRequest.url().toString()),
                Instant.now(clock).plus(properties.getPresignDownloadTtl()),
                Map.of());
    }

    @Override
    public String readObjectPreview(String objectKey, int maxBytes) {
        requireObjectKey(objectKey);
        if (maxBytes < 1) {
            throw new IllegalArgumentException("maxBytes must be positive");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .range("bytes=0-%d".formatted(maxBytes - 1))
                .build();
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(getObjectRequest);
        return response.asUtf8String();
    }

    private static void requireObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("objectKey is required");
        }
    }

    private static void requireContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("contentType is required");
        }
    }
}
