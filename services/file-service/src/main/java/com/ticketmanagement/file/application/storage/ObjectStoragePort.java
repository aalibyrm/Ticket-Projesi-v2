package com.ticketmanagement.file.application.storage;

public interface ObjectStoragePort {

    PresignedObjectOperation createUploadUrl(String objectKey, String contentType);

    PresignedObjectOperation createDownloadUrl(String objectKey);

    String readObjectPreview(String objectKey, int maxBytes);
}
