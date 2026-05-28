package com.ticketmanagement.workflow.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.outbox.publisher")
public class OutboxPublisherProperties {

    private boolean enabled = true;

    @Min(1)
    private int batchSize = 50;

    @Min(1)
    private int maxRetries = 5;

    @Min(1000)
    private long fixedDelayMs = 5000;

    @Min(0)
    private long initialDelayMs = 10000;

    @Min(1000)
    private long retryBackoffMs = 30000;

    @Min(1000)
    private long processingTimeoutMs = 300000;

    @Min(1000)
    private long sendTimeoutMs = 10000;
}
