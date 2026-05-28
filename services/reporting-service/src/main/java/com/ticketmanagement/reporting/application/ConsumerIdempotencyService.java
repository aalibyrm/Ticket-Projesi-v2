package com.ticketmanagement.reporting.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.reporting.infrastructure.kafka.ConsumedEvent;
import com.ticketmanagement.reporting.infrastructure.persistence.ProcessedEventStore;

@Service
@RequiredArgsConstructor
public class ConsumerIdempotencyService {

    private final ProcessedEventStore processedEventStore;

    // Event side effect'ini yalnizca ilk delivery icin calistirir.
    @Transactional
    public boolean processOnce(String consumerName, ConsumedEvent event, Runnable sideEffect) {
        boolean firstDelivery = processedEventStore.insertIfAbsent(consumerName, event);
        if (!firstDelivery) {
            return false;
        }
        sideEffect.run();
        return true;
    }
}
