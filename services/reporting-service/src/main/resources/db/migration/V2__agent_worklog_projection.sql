CREATE TABLE IF NOT EXISTS agent_worklog_projection (
  worklog_id UUID PRIMARY KEY,
  ticket_id UUID NOT NULL,
  ticket_number VARCHAR(32) NOT NULL,
  agent_id UUID NOT NULL,
  work_date DATE NOT NULL,
  duration_minutes INTEGER NOT NULL,
  projected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT agent_worklog_duration_positive_check CHECK (duration_minutes > 0)
);

CREATE INDEX IF NOT EXISTS idx_agent_worklog_agent_work_date
  ON agent_worklog_projection (agent_id, work_date DESC);

CREATE INDEX IF NOT EXISTS idx_agent_worklog_ticket_id
  ON agent_worklog_projection (ticket_id);
