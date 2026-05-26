package com.ticketmanagement.file.application;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.ticketmanagement.file.api.dto.CreateUploadUrlRequest;
import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import com.ticketmanagement.file.config.FileValidationProperties;
import com.ticketmanagement.file.domain.FileValidationStatus;

@Service
@RequiredArgsConstructor
public class FileValidationService {

    private static final Map<String, Set<String>> EXTENSION_CONTENT_TYPES = Map.of(
            "log", Set.of("text/plain"),
            "txt", Set.of("text/plain"),
            "png", Set.of("image/png"),
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "pdf", Set.of("application/pdf"));

    private final FileValidationProperties properties;
    private final ObjectProvider<ObjectStoragePort> objectStoragePortProvider;

    // Upload URL verilmeden once dosya adi, uzanti, MIME ipucu ve boyut kurallarini uygular.
    public void validateUploadRequest(CreateUploadUrlRequest request) {
        String extension = extensionOf(request.originalFilename());
        String contentType = normalizeContentType(request.contentType());

        if (request.sizeBytes() > properties.getMaxSizeBytes()) {
            throw new FileValidationException("File size exceeds allowed limit");
        }
        if (!containsNormalized(properties.getAllowedExtensions(), extension)) {
            throw new FileValidationException("File extension is not allowed");
        }
        if (!containsNormalized(properties.getAllowedContentTypes(), contentType)) {
            throw new FileValidationException("File content type is not allowed");
        }
        if (!contentTypeMatchesExtension(extension, contentType)) {
            throw new FileValidationException("File content type does not match extension");
        }
    }

    // Upload tamamlandiktan sonra dosyanin validation status degerini hesaplar.
    public FileValidationStatus validateCompletedUpload(FileMetadataResponse metadata) {
        if (!isTextLog(metadata)) {
            return FileValidationStatus.VALIDATED;
        }

        try {
            String preview = objectStoragePort().readObjectPreview(metadata.objectKey(), properties.getPreviewMaxBytes());
            return containsRequiredKeyword(preview) ? FileValidationStatus.VALIDATED : FileValidationStatus.REJECTED;
        } catch (RuntimeException exception) {
            return FileValidationStatus.FAILED;
        }
    }

    // Dosya adi icinden guvenli ve normalize edilmis uzantiyi cikarir.
    private String extensionOf(String originalFilename) {
        String filename = originalFilename == null ? "" : originalFilename.trim();
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\") || hasControlCharacter(filename)) {
            throw new FileValidationException("File name is invalid");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 1 || dotIndex == filename.length() - 1) {
            throw new FileValidationException("File extension is required");
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    // Content-Type degerini parametrelerden arindirip normalize eder.
    private String normalizeContentType(String contentType) {
        String value = contentType == null ? "" : contentType.trim();
        int parameterIndex = value.indexOf(';');
        if (parameterIndex >= 0) {
            value = value.substring(0, parameterIndex);
        }
        if (value.isBlank()) {
            throw new FileValidationException("File content type is required");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    // MIME ipucunun uzanti ile uyumlu olup olmadigini kontrol eder.
    private boolean contentTypeMatchesExtension(String extension, String contentType) {
        Set<String> expectedContentTypes = EXTENSION_CONTENT_TYPES.get(extension);
        return expectedContentTypes == null || expectedContentTypes.contains(contentType);
    }

    // Text log kurallarinin uygulanip uygulanmayacagini belirler.
    private boolean isTextLog(FileMetadataResponse metadata) {
        String extension = extensionOf(metadata.originalFilename());
        String contentType = normalizeContentType(metadata.contentType());
        return containsNormalized(properties.getTextExtensions(), extension)
                || containsNormalized(properties.getTextContentTypes(), contentType);
    }

    // Preview icinde guvenli log anahtar kelimelerinden en az birini arar.
    private boolean containsRequiredKeyword(String preview) {
        if (preview == null || preview.isBlank()) {
            return false;
        }
        String normalizedPreview = preview.toLowerCase(Locale.ROOT);
        return properties.getLogKeywords()
                .stream()
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedPreview::contains);
    }

    // Liste icinde normalize edilmis deger arar.
    private boolean containsNormalized(java.util.Collection<String> values, String expected) {
        return values.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(expected::equals);
    }

    // Dosya adinda kontrol karakteri olup olmadigini kontrol eder.
    private boolean hasControlCharacter(String value) {
        return value.chars().anyMatch(Character::isISOControl);
    }

    // Konfigure edilmis object storage adapterini getirir.
    private ObjectStoragePort objectStoragePort() {
        ObjectStoragePort objectStoragePort = objectStoragePortProvider.getIfAvailable();
        if (objectStoragePort == null) {
            throw new StorageUnavailableException();
        }
        return objectStoragePort;
    }
}
