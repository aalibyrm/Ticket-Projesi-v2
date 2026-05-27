package com.ticketmanagement.ticket.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
class OutboxPublisherJob {

    private final OutboxPublisherService outboxPublisherService;

    // Belirli araliklarla outbox eventlerini Kafka'ya yayinlamayi dener.
    @Scheduled(
            initialDelayString = "${app.outbox.publisher.initial-delay-ms:10000}",
            fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    void publishPendingEvents() {
        outboxPublisherService.publishPendingBatch();
    }
}
