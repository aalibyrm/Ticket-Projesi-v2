CREATE TABLE IF NOT EXISTS service_metadata (
    metadata_key VARCHAR(120) PRIMARY KEY,
    metadata_value VARCHAR(500) NOT NULL
);

INSERT INTO service_metadata (metadata_key, metadata_value)
VALUES
    ('service_name', 'workflow-sla-service'),
    ('workflow_runtime', 'kogito')
ON CONFLICT (metadata_key) DO UPDATE
SET metadata_value = EXCLUDED.metadata_value;
