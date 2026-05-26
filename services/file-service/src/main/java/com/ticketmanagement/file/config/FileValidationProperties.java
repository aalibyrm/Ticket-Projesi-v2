package com.ticketmanagement.file.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.file-validation")
public class FileValidationProperties {

    private long maxSizeBytes = 10 * 1024 * 1024;
    private int previewMaxBytes = 64 * 1024;
    private List<String> allowedExtensions = new ArrayList<>(List.of("log", "txt", "png", "jpg", "jpeg", "pdf"));
    private List<String> allowedContentTypes = new ArrayList<>(List.of(
            "text/plain",
            "image/png",
            "image/jpeg",
            "application/pdf"));
    private List<String> textExtensions = new ArrayList<>(List.of("log", "txt"));
    private List<String> textContentTypes = new ArrayList<>(List.of("text/plain"));
    private List<String> logKeywords = new ArrayList<>(List.of("error", "exception", "stacktrace", "traceback", "failed"));
}
