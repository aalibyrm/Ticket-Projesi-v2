ALTER TABLE ticket_report_projection
  ADD COLUMN IF NOT EXISTS topic_code VARCHAR(80),
  ADD COLUMN IF NOT EXISTS topic_name VARCHAR(160),
  ADD COLUMN IF NOT EXISTS routed_department_id UUID,
  ADD COLUMN IF NOT EXISTS routed_department_code VARCHAR(80),
  ADD COLUMN IF NOT EXISTS routed_department_name VARCHAR(160);

CREATE INDEX IF NOT EXISTS idx_ticket_report_department_status
  ON ticket_report_projection (routed_department_id, status)
  WHERE routed_department_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_ticket_report_topic_status
  ON ticket_report_projection (topic_code, status)
  WHERE topic_code IS NOT NULL;
