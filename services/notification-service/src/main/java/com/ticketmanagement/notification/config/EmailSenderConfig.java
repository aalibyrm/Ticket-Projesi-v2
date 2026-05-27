package com.ticketmanagement.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailSenderProperties.class)
class EmailSenderConfig {
}
