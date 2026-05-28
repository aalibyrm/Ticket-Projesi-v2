CREATE TABLE IF NOT EXISTS processed_events (
    event_id UUID NOT NULL,
    consumer_name VARCHAR(160) NOT NULL,
    event_type VARCHAR(160) NOT NULL,
    event_version INTEGER NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer_name)
);

CREATE INDEX IF NOT EXISTS idx_processed_events_type_processed_at
    ON processed_events (event_type, processed_at);

CREATE TABLE IF NOT EXISTS sla_ticket_states (
    ticket_id UUID PRIMARY KEY,
    ticket_number VARCHAR(32) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    target_resolution_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT sla_ticket_states_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT sla_ticket_states_status_check CHECK (status IN ('ACTIVE', 'AT_RISK', 'BREACHED', 'MET'))
);

CREATE INDEX IF NOT EXISTS idx_sla_ticket_states_status_target
    ON sla_ticket_states (status, target_resolution_at);

CREATE INDEX IF NOT EXISTS idx_sla_ticket_states_priority
    ON sla_ticket_states (priority);
