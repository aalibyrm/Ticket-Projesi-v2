package com.ticketmanagement.workflow.config;

import java.time.Duration;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.ticketmanagement.workflow.domain.SlaPriority;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.sla.detection")
public class SlaDetectionProperties {

    private static final UUID DEFAULT_SYSTEM_ACTOR_ID = new UUID(0L, 0L);

    private boolean enabled = true;

    @Min(1)
    private int batchSize = 50;

    @Min(1000)
    private long fixedDelayMs = 60000;

    @Min(0)
    private long initialDelayMs = 15000;

    @NotNull
    @DurationMin(seconds = 1)
    private Duration highRiskWindow = Duration.ofHours(2);

    @NotNull
    @DurationMin(seconds = 1)
    private Duration mediumRiskWindow = Duration.ofHours(4);

    @NotNull
    @DurationMin(seconds = 1)
    private Duration lowRiskWindow = Duration.ofHours(12);

    @NotNull
    private UUID systemActorId = DEFAULT_SYSTEM_ACTOR_ID;

    public Duration riskWindow(SlaPriority priority) {
        return switch (priority) {
            case HIGH -> highRiskWindow;
            case MEDIUM -> mediumRiskWindow;
            case LOW -> lowRiskWindow;
        };
    }
}
