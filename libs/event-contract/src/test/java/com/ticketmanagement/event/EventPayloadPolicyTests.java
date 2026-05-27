package com.ticketmanagement.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EventPayloadPolicyTests {

    @Test
    void rejectsSensitivePayloadFieldNamesAcrossCommonNamingStyles() {
        assertTrue(EventPayloadPolicy.isForbiddenFieldName("accessToken"));
        assertTrue(EventPayloadPolicy.isForbiddenFieldName("access_token"));
        assertTrue(EventPayloadPolicy.isForbiddenFieldName("presigned-url"));
        assertTrue(EventPayloadPolicy.isForbiddenFieldName("object.key"));
    }

    @Test
    void allowsNonSensitiveIdentifierFields() {
        assertFalse(EventPayloadPolicy.isForbiddenFieldName("ticketId"));
        assertFalse(EventPayloadPolicy.isForbiddenFieldName("status"));
        assertFalse(EventPayloadPolicy.isForbiddenFieldName("priority"));
    }

    @Test
    void throwsWhenForbiddenFieldNameIsRequiredAsAllowed() {
        assertThrows(IllegalArgumentException.class, () -> EventPayloadPolicy.requireAllowedFieldName("password"));
    }
}
