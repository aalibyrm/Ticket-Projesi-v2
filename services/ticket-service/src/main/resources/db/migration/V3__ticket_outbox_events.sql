CREATE TABLE IF NOT EXISTS outbox_events (
  id UUID PRIMARY KEY,
  topic_name VARCHAR(160) NOT NULL,
  event_type VARCHAR(160) NOT NULL,
  event_version INTEGER NOT NULL,
  aggregate_type VARCHAR(80) NOT NULL,
  aggregate_id UUID NOT NULL,
  actor_id UUID NOT NULL,
  correlation_id VARCHAR(160),
  occurred_at TIMESTAMPTZ NOT NULL,
  payload JSONB NOT NULL,
  status VARCHAR(20) NOT NULL,
  retry_count INTEGER NOT NULL DEFAULT 0,
  last_error VARCHAR(1000),
  next_attempt_at TIMESTAMPTZ,
  published_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT outbox_events_status_check CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
  CONSTRAINT outbox_events_retry_count_check CHECK (retry_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_pending
  ON outbox_events (status, next_attempt_at, created_at)
  WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate
  ON outbox_events (aggregate_type, aggregate_id, created_at);
