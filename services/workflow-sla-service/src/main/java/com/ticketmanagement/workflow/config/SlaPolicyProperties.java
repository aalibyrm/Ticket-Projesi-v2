package com.ticketmanagement.workflow.config;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.sla.policies")
public class SlaPolicyProperties {

    @NotNull
    @DurationMin(seconds = 1)
    private Duration lowTargetResolution = Duration.ofHours(72);

    @NotNull
    @DurationMin(seconds = 1)
    private Duration mediumTargetResolution = Duration.ofHours(24);

    @NotNull
    @DurationMin(seconds = 1)
    private Duration highTargetResolution = Duration.ofHours(8);
}
