package com.ticketmanagement.workflow.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SlaPolicyProperties.class)
class SlaPolicyConfig {
}
