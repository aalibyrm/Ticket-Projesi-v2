package com.ticketmanagement.ticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class ReportingServiceClientConfig {

    @Bean
    RestClient reportingServiceRestClient(
            RestClient.Builder builder,
            @Value("${app.clients.reporting-service.base-url:http://localhost:8085}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
