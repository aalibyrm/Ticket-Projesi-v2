package com.ticketmanagement.notification.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.email.delivery.retry")
public class EmailRetryProperties {

    private boolean enabled = true;

    private int batchSize = 25;

    private int maxAttempts = 3;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration backoff = Duration.ofMinutes(1);

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration processingLease = Duration.ofMinutes(5);
}
