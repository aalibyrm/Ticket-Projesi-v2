ALTER TABLE tickets
  ADD COLUMN IF NOT EXISTS assignee_id UUID,
  ADD COLUMN IF NOT EXISTS assigned_team_id UUID;

CREATE INDEX IF NOT EXISTS idx_tickets_assignee ON tickets (assignee_id);
CREATE INDEX IF NOT EXISTS idx_tickets_assigned_team ON tickets (assigned_team_id);

CREATE TABLE IF NOT EXISTS ticket_comments (
  id UUID PRIMARY KEY,
  ticket_id UUID NOT NULL REFERENCES tickets(id),
  author_id UUID NOT NULL,
  visibility VARCHAR(20) NOT NULL,
  body VARCHAR(5000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ticket_comments_visibility_check CHECK (visibility IN ('INTERNAL', 'EXTERNAL'))
);

CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket_created_at
  ON ticket_comments (ticket_id, created_at);

CREATE TABLE IF NOT EXISTS ticket_worklogs (
  id UUID PRIMARY KEY,
  ticket_id UUID NOT NULL REFERENCES tickets(id),
  agent_id UUID NOT NULL,
  work_date DATE NOT NULL,
  duration_minutes INTEGER NOT NULL,
  description VARCHAR(2000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ticket_worklogs_duration_check CHECK (duration_minutes > 0 AND duration_minutes <= 1440)
);

CREATE INDEX IF NOT EXISTS idx_ticket_worklogs_ticket_created_at
  ON ticket_worklogs (ticket_id, created_at);

CREATE INDEX IF NOT EXISTS idx_ticket_worklogs_agent_work_date
  ON ticket_worklogs (agent_id, work_date);
