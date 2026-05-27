package com.ticketmanagement.event;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class EventPayloadPolicy {

    private static final Set<String> FORBIDDEN_FIELD_NAMES = Set.of(
            "accessToken",
            "authorization",
            "fileContent",
            "objectKey",
            "password",
            "presignedUrl",
            "privateKey",
            "refreshToken",
            "secret",
            "token");

    private static final Set<String> NORMALIZED_FORBIDDEN_FIELD_NAMES = FORBIDDEN_FIELD_NAMES.stream()
            .map(EventPayloadPolicy::normalize)
            .collect(Collectors.toUnmodifiableSet());

    private EventPayloadPolicy() {
    }

    public static Set<String> forbiddenFieldNames() {
        return FORBIDDEN_FIELD_NAMES;
    }

    public static boolean isForbiddenFieldName(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }
        return NORMALIZED_FORBIDDEN_FIELD_NAMES.contains(normalize(fieldName));
    }

    public static void requireAllowedFieldName(String fieldName) {
        if (isForbiddenFieldName(fieldName)) {
            throw new IllegalArgumentException("Event payload field is forbidden: " + fieldName);
        }
    }

    private static String normalize(String value) {
        return value.replace("_", "")
                .replace("-", "")
                .replace(".", "")
                .toLowerCase(Locale.ROOT);
    }
}
