package com.ticketmanagement.event;

public enum EventTopic {

    TICKET_EVENTS("ticket.events.v1"),
    FILE_EVENTS("file.events.v1"),
    WORKFLOW_EVENTS("workflow.events.v1"),
    NOTIFICATION_EVENTS("notification.events.v1");

    private final String topicName;

    EventTopic(String topicName) {
        this.topicName = topicName;
    }

    public String topicName() {
        return topicName;
    }
}
