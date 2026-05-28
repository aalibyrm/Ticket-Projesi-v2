package com.ticketmanagement.workflow.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.workflow.config.SlaDetectionProperties;
import com.ticketmanagement.workflow.infrastructure.persistence.SlaTicketStateEntity;
import com.ticketmanagement.workflow.infrastructure.persistence.SlaTicketStateJpaRepository;

@Service
@RequiredArgsConstructor
public class SlaDetectionService {

    private final SlaDetectionProperties detectionProperties;
    private final SlaTicketStateJpaRepository slaTicketStateRepository;
    private final WorkflowSlaOutboxService workflowSlaOutboxService;

    // Guncel UTC saate gore risk ve breach detection taramasini calistirir.
    @Transactional
    public int detectDueSlaEvents() {
        return detectDueSlaEvents(OffsetDateTime.now(ZoneOffset.UTC));
    }

    // Verilen detection zamanina gore risk ve breach eventlerini idempotent uretir.
    @Transactional
    public int detectDueSlaEvents(OffsetDateTime detectedAt) {
        int breachCount = detectBreaches(detectedAt);
        int riskCount = detectRisks(detectedAt);
        return breachCount + riskCount;
    }

    // Deadline'i gecen aktif veya riskli SLA state'leri breach eventine cevirir.
    private int detectBreaches(OffsetDateTime detectedAt) {
        List<SlaTicketStateEntity> candidates = slaTicketStateRepository.findBreachCandidatesForUpdate(
                detectedAt,
                detectionProperties.getBatchSize());
        int eventCount = 0;
        for (SlaTicketStateEntity state : candidates) {
            if (state.markBreached(detectedAt)) {
                workflowSlaOutboxService.saveSlaBreached(state, detectedAt);
                eventCount++;
            }
        }
        return eventCount;
    }

    // Priority bazli risk penceresine giren aktif SLA state'leri risk eventine cevirir.
    private int detectRisks(OffsetDateTime detectedAt) {
        List<SlaTicketStateEntity> candidates = slaTicketStateRepository.findRiskCandidatesForUpdate(
                detectedAt,
                detectedAt.plus(detectionProperties.getHighRiskWindow()),
                detectedAt.plus(detectionProperties.getMediumRiskWindow()),
                detectedAt.plus(detectionProperties.getLowRiskWindow()),
                detectionProperties.getBatchSize());
        int eventCount = 0;
        for (SlaTicketStateEntity state : candidates) {
            if (state.markAtRisk(detectedAt)) {
                workflowSlaOutboxService.saveSlaRiskDetected(state, detectedAt);
                eventCount++;
            }
        }
        return eventCount;
    }
}
