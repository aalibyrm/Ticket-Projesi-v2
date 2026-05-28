package com.ticketmanagement.event;

public enum EventType {

    TICKET_CREATED("ticket.created", EventTopic.TICKET_EVENTS, 1, "ticket"),
    TICKET_STATUS_CHANGED("ticket.status-changed", EventTopic.TICKET_EVENTS, 1, "ticket"),
    TICKET_ASSIGNED("ticket.assigned", EventTopic.TICKET_EVENTS, 1, "ticket"),
    TICKET_EXTERNAL_COMMENT_ADDED("ticket.external-comment-added", EventTopic.TICKET_EVENTS, 1, "ticket"),
    TICKET_WORKLOG_ADDED("ticket.worklog-added", EventTopic.TICKET_EVENTS, 1, "ticket"),
    FILE_ATTACHMENT_ADDED("file.attachment-added", EventTopic.FILE_EVENTS, 1, "attachment"),
    WORKFLOW_SLA_RISK_DETECTED("workflow.sla-risk-detected", EventTopic.WORKFLOW_EVENTS, 1, "sla"),
    WORKFLOW_SLA_BREACH_DETECTED("workflow.sla-breach-detected", EventTopic.WORKFLOW_EVENTS, 1, "sla"),
    NOTIFICATION_EMAIL_SENT("notification.email-sent", EventTopic.NOTIFICATION_EVENTS, 1, "notification"),
    NOTIFICATION_EMAIL_FAILED("notification.email-failed", EventTopic.NOTIFICATION_EVENTS, 1, "notification");

    private final String eventName;
    private final EventTopic topic;
    private final int version;
    private final String aggregateType;

    EventType(String eventName, EventTopic topic, int version, String aggregateType) {
        this.eventName = eventName;
        this.topic = topic;
        this.version = version;
        this.aggregateType = aggregateType;
    }

    public String eventName() {
        return eventName;
    }

    public EventTopic topic() {
        return topic;
    }

    public int version() {
        return version;
    }

    public String aggregateType() {
        return aggregateType;
    }
}
