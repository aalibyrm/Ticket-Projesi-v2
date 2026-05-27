CREATE TABLE IF NOT EXISTS email_deliveries (
  id UUID PRIMARY KEY,
  source_event_id UUID NOT NULL,
  recipient_email VARCHAR(320) NOT NULL,
  subject VARCHAR(180) NOT NULL,
  template_key VARCHAR(120) NOT NULL,
  template_model JSONB NOT NULL,
  status VARCHAR(30) NOT NULL,
  retry_count INTEGER NOT NULL DEFAULT 0,
  last_error VARCHAR(1000),
  next_attempt_at TIMESTAMPTZ,
  sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT email_deliveries_status_check CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
  CONSTRAINT email_deliveries_retry_count_check CHECK (retry_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_email_deliveries_status_next_attempt
  ON email_deliveries (status, next_attempt_at, created_at);

CREATE INDEX IF NOT EXISTS idx_email_deliveries_source_event
  ON email_deliveries (source_event_id);
