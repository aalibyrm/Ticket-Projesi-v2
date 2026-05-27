package com.ticketmanagement.ticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class FileServiceClientConfig {

    @Bean
    RestClient fileServiceRestClient(
            RestClient.Builder builder,
            @Value("${app.clients.file-service.base-url:http://localhost:8082}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
