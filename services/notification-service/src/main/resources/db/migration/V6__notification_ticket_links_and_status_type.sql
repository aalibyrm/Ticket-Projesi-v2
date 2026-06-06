ALTER TABLE notifications
  ADD COLUMN IF NOT EXISTS ticket_id UUID;

CREATE INDEX IF NOT EXISTS idx_notifications_ticket_id
  ON notifications (ticket_id);

ALTER TABLE notifications
  DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications
  ADD CONSTRAINT notifications_type_check
  CHECK (type IN ('TICKET_CREATED', 'TICKET_EXTERNAL_COMMENT_ADDED', 'TICKET_STATUS_CHANGED', 'SLA_RISK', 'SLA_BREACH'));
