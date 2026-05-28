package com.ticketmanagement.workflow.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({
        SlaPolicyProperties.class,
        SlaDetectionProperties.class,
        OutboxPublisherProperties.class
})
class SlaPolicyConfig {
}
