package com.ticketmanagement.reporting.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ticketmanagement.reporting.infrastructure.kafka.ConsumedEvent;

@Repository
@RequiredArgsConstructor
public class ProcessedEventStore {

    private final JdbcTemplate jdbcTemplate;

    public boolean insertIfAbsent(String consumerName, ConsumedEvent event) {
        int inserted = jdbcTemplate.update(
                """
                        insert into reporting_schema.processed_events (
                          event_id,
                          consumer_name,
                          event_type,
                          event_version,
                          aggregate_type,
                          aggregate_id,
                          processed_at
                        )
                        values (?, ?, ?, ?, ?, ?, now())
                        on conflict (event_id, consumer_name) do nothing
                        """,
                event.eventId(),
                consumerName,
                event.eventType(),
                event.version(),
                event.aggregateType(),
                event.aggregateId());
        return inserted == 1;
    }
}
