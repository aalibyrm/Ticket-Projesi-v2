package com.ticketmanagement.workflow.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ticketmanagement.workflow.application.SlaDetectionService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.sla.detection", name = "enabled", havingValue = "true", matchIfMissing = true)
class SlaDetectionJob {

    private final SlaDetectionService slaDetectionService;

    // Belirli araliklarla SLA risk ve breach durumlarini tarar.
    @Scheduled(
            initialDelayString = "${app.sla.detection.initial-delay-ms:15000}",
            fixedDelayString = "${app.sla.detection.fixed-delay-ms:60000}")
    void detectDueSlaEvents() {
        slaDetectionService.detectDueSlaEvents();
    }
}
