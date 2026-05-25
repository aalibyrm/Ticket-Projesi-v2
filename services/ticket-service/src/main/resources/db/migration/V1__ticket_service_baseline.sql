CREATE TABLE IF NOT EXISTS service_metadata (
  metadata_key VARCHAR(100) PRIMARY KEY,
  metadata_value VARCHAR(500) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO service_metadata (metadata_key, metadata_value)
VALUES ('service_name', 'ticket-service')
ON CONFLICT (metadata_key) DO NOTHING;

