package com.ticketmanagement.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class TicketServiceClientConfig {

    @Bean
    RestClient ticketServiceRestClient(
            RestClient.Builder builder,
            @Value("${app.clients.ticket-service.base-url:http://localhost:8081}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
