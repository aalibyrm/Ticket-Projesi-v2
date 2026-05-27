CREATE TABLE IF NOT EXISTS service_metadata (
  metadata_key VARCHAR(120) PRIMARY KEY,
  metadata_value VARCHAR(500) NOT NULL
);

INSERT INTO service_metadata (metadata_key, metadata_value)
VALUES ('service_name', 'notification-service')
ON CONFLICT (metadata_key) DO NOTHING;

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

CREATE TABLE IF NOT EXISTS notifications (
  id UUID PRIMARY KEY,
  source_event_id UUID NOT NULL UNIQUE,
  recipient_id UUID NOT NULL,
  type VARCHAR(60) NOT NULL,
  title VARCHAR(180) NOT NULL,
  message VARCHAR(500) NOT NULL,
  read_flag BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT notifications_type_check CHECK (type IN ('TICKET_CREATED'))
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created_at
  ON notifications (recipient_id, created_at DESC);
