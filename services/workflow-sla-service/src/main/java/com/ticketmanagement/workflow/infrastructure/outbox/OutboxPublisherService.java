package com.ticketmanagement.workflow.infrastructure.outbox;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketmanagement.workflow.config.OutboxPublisherProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxEventJpaRepository outboxEventRepository;
    private final OutboxPublisherProperties properties;
    private final TransactionTemplate transactionTemplate;

    // Claim edilebilir SLA outbox eventlerini Kafka'ya yayinlar ve sonuc durumunu kaydeder.
    public int publishPendingBatch() {
        List<OutboxEventEntity> events = transactionTemplate.execute(status -> claimNextBatch());
        if (events == null || events.isEmpty()) {
            return 0;
        }

        int publishedCount = 0;
        for (OutboxEventEntity event : events) {
            try {
                publishToKafka(event);
                transactionTemplate.executeWithoutResult(status -> markPublished(event.getId()));
                publishedCount++;
            } catch (RuntimeException exception) {
                log.warn(
                        "Workflow outbox event publish failed. eventId={} eventType={} error={}",
                        event.getId(),
                        event.getEventType(),
                        rootMessage(exception));
                log.debug("Workflow outbox event publish failure stacktrace. eventId={}", event.getId(), exception);
                transactionTemplate.executeWithoutResult(status -> markFailed(event.getId(), exception));
            }
        }
        return publishedCount;
    }

    // Publish icin uygun SLA outbox kayitlarini database lock ile claim eder.
    private List<OutboxEventEntity> claimNextBatch() {
        List<OutboxEventEntity> events = outboxEventRepository.findClaimableForUpdate(
                properties.getMaxRetries(),
                properties.getBatchSize());
        OffsetDateTime lockedUntil = now().plus(Duration.ofMillis(properties.getProcessingTimeoutMs()));
        events.forEach(event -> event.markProcessing(lockedUntil));
        outboxEventRepository.flush();
        return List.copyOf(events);
    }

    // Tek bir SLA outbox kaydini aggregate id key'i ile Kafka topic'ine gonderir.
    private void publishToKafka(OutboxEventEntity event) {
        String message = serialize(event);
        try {
            kafkaTemplate.send(event.getTopicName(), event.getAggregateId().toString(), message)
                    .get(properties.getSendTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Outbox publish interrupted", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Outbox publish failed", exception);
        }
    }

    // SLA outbox kaydini Kafka'ya gidecek envelope JSON'una cevirir.
    private String serialize(OutboxEventEntity event) {
        try {
            return objectMapper.writeValueAsString(new KafkaEventEnvelope(
                    event.getId(),
                    event.getEventType(),
                    event.getEventVersion(),
                    event.getOccurredAt(),
                    event.getActorId(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    event.getCorrelationId(),
                    event.getPayload()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Outbox event serialization failed", exception);
        }
    }

    // Basarili publish sonrasi SLA outbox kaydini tamamlanmis olarak isaretler.
    private void markPublished(UUID eventId) {
        outboxEventRepository.findById(eventId)
                .ifPresent(event -> event.markPublished(now()));
    }

    // Basarisiz publish sonrasi retry state'ini ve backoff zamanini kaydeder.
    private void markFailed(UUID eventId, RuntimeException exception) {
        outboxEventRepository.findById(eventId)
                .ifPresent(event -> {
                    int nextRetryCount = event.getRetryCount() + 1;
                    OffsetDateTime nextAttemptAt = nextRetryCount >= properties.getMaxRetries()
                            ? null
                            : now().plus(Duration.ofMillis(properties.getRetryBackoffMs() * nextRetryCount));
                    event.markFailed(rootMessage(exception), nextAttemptAt);
                });
    }

    // Outbox state timestamp'leri icin UTC zaman uretir.
    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    // Log ve last_error icin en alttaki hata mesajini dondurur.
    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record KafkaEventEnvelope(
            UUID eventId,
            String eventType,
            int version,
            OffsetDateTime occurredAt,
            UUID actorId,
            String aggregateType,
            UUID aggregateId,
            String correlationId,
            JsonNode payload) {
    }
}
