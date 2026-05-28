package com.ticketmanagement.workflow.application;

import java.time.Duration;
import java.time.OffsetDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.workflow.config.SlaPolicyProperties;
import com.ticketmanagement.workflow.domain.SlaPriority;

@Service
@RequiredArgsConstructor
public class SlaPolicyService {

    private final SlaPolicyProperties properties;

    // Ticket priority degerine gore hedef cozum deadline'ini hesaplar.
    public OffsetDateTime targetResolutionAt(SlaPriority priority, OffsetDateTime openedAt) {
        return openedAt.plus(targetResolutionDuration(priority));
    }

    // Config tabanli SLA sure politikasini priority'ye gore dondurur.
    public Duration targetResolutionDuration(SlaPriority priority) {
        return switch (priority) {
            case LOW -> properties.getLowTargetResolution();
            case MEDIUM -> properties.getMediumTargetResolution();
            case HIGH -> properties.getHighTargetResolution();
        };
    }
}
