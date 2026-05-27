ALTER TABLE outbox_events
  DROP CONSTRAINT IF EXISTS outbox_events_status_check;

ALTER TABLE outbox_events
  ADD CONSTRAINT outbox_events_status_check
  CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED'));

DROP INDEX IF EXISTS idx_outbox_events_pending;

CREATE INDEX IF NOT EXISTS idx_outbox_events_claimable
  ON outbox_events (status, next_attempt_at, created_at)
  WHERE status IN ('PENDING', 'PROCESSING', 'FAILED');
