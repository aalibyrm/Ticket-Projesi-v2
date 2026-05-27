package com.ticketmanagement.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EventTopicTests {

    @Test
    void exposesVersionedKafkaTopicNames() {
        assertEquals("ticket.events.v1", EventTopic.TICKET_EVENTS.topicName());
        assertEquals("file.events.v1", EventTopic.FILE_EVENTS.topicName());
        assertEquals("workflow.events.v1", EventTopic.WORKFLOW_EVENTS.topicName());
        assertEquals("notification.events.v1", EventTopic.NOTIFICATION_EVENTS.topicName());
    }

    @Test
    void allEventTypesDeclareSupportedVersionAndTopic() {
        for (EventType eventType : EventType.values()) {
            assertTrue(EventVersionPolicy.isSupported(eventType.version()));
            assertTrue(eventType.topic().topicName().endsWith(".v" + eventType.version()));
        }
    }
}
