ALTER TABLE notifications
  DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications
  ADD CONSTRAINT notifications_type_check
  CHECK (type IN ('TICKET_CREATED', 'TICKET_EXTERNAL_COMMENT_ADDED', 'SLA_RISK', 'SLA_BREACH'));
