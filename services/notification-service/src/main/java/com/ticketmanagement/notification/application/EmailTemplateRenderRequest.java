package com.ticketmanagement.notification.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ticketmanagement.notification.domain.EmailTemplateKey;

public record EmailTemplateRenderRequest(
        EmailTemplateKey templateKey,
        Map<String, Object> model) {

    private static final Set<String> SENSITIVE_MODEL_KEYS = Set.of(
            "internalnote",
            "internalnotes",
            "internalcomment",
            "internalcomments",
            "privatecomment",
            "privatecomments",
            "worklog",
            "worklogs");

    public EmailTemplateRenderRequest {
        templateKey = Objects.requireNonNull(templateKey, "templateKey must not be null");
        model = sanitizeModel(model == null ? Map.of() : model);
    }

    private static Map<String, Object> sanitizeModel(Map<?, ?> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String modelKey = Objects.toString(key, "");
            if (!isSensitive(modelKey)) {
                sanitized.put(modelKey, sanitizeValue(value));
            }
        });
        return Collections.unmodifiableMap(sanitized);
    }

    private static Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> nestedMap) {
            return sanitizeModel(nestedMap);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(EmailTemplateRenderRequest::sanitizeValue)
                    .toList();
        }
        if (value instanceof Object[] array) {
            ArrayList<Object> sanitized = new ArrayList<>(array.length);
            for (Object item : array) {
                sanitized.add(sanitizeValue(item));
            }
            return Collections.unmodifiableList(sanitized);
        }
        return value;
    }

    private static boolean isSensitive(String modelKey) {
        String normalized = modelKey.replace("_", "")
                .replace("-", "")
                .toLowerCase(Locale.ROOT);
        return SENSITIVE_MODEL_KEYS.contains(normalized);
    }
}
