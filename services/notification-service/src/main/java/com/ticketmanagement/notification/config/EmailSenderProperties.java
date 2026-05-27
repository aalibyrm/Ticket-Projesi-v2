package com.ticketmanagement.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.email.sender")
public class EmailSenderProperties {

    private String fromAddress = "no-reply@ticket.local";
    private String fromName = "Ticket Management";
}
