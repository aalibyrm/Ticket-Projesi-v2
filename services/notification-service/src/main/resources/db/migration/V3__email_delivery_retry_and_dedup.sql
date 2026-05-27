ALTER TABLE email_deliveries
  DROP CONSTRAINT IF EXISTS email_deliveries_status_check;

ALTER TABLE email_deliveries
  ADD CONSTRAINT email_deliveries_status_check
  CHECK (status IN ('PENDING', 'RETRYING', 'SENT', 'FAILED'));

CREATE UNIQUE INDEX IF NOT EXISTS ux_email_deliveries_event_template_recipient
  ON email_deliveries (source_event_id, template_key, lower(recipient_email));
