CREATE TABLE IF NOT EXISTS service_metadata (
  metadata_key VARCHAR(120) PRIMARY KEY,
  metadata_value VARCHAR(500) NOT NULL
);

INSERT INTO service_metadata (metadata_key, metadata_value)
VALUES ('service_name', 'reporting-service')
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

CREATE INDEX IF NOT EXISTS idx_reporting_processed_events_type_processed_at
  ON processed_events (event_type, processed_at);

CREATE TABLE IF NOT EXISTS ticket_report_projection (
  ticket_id UUID PRIMARY KEY,
  ticket_number VARCHAR(32) NOT NULL UNIQUE,
  customer_id UUID NOT NULL,
  product_id UUID NOT NULL,
  priority VARCHAR(20) NOT NULL,
  status VARCHAR(40) NOT NULL,
  assignee_id UUID,
  assigned_team_id UUID,
  opened_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  closed_at TIMESTAMPTZ,
  sla_target_resolution_at TIMESTAMPTZ,
  sla_status VARCHAR(40),
  projected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ticket_report_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
  CONSTRAINT ticket_report_status_check CHECK (
    status IN ('NEW', 'IN_PROGRESS', 'WAITING_FOR_CUSTOMER', 'RESOLVED', 'CLOSED')
  ),
  CONSTRAINT ticket_report_sla_status_check CHECK (
    sla_status IS NULL OR sla_status IN ('ACTIVE', 'AT_RISK', 'BREACHED', 'MET')
  ),
  CONSTRAINT ticket_report_closed_at_check CHECK (status <> 'CLOSED' OR closed_at IS NOT NULL),
  CONSTRAINT ticket_report_updated_at_check CHECK (updated_at >= opened_at),
  CONSTRAINT ticket_report_closed_after_open_check CHECK (closed_at IS NULL OR closed_at >= opened_at)
);

CREATE INDEX IF NOT EXISTS idx_ticket_report_status_opened_at
  ON ticket_report_projection (status, opened_at);

CREATE INDEX IF NOT EXISTS idx_ticket_report_priority_status
  ON ticket_report_projection (priority, status);

CREATE INDEX IF NOT EXISTS idx_ticket_report_assignee_status
  ON ticket_report_projection (assignee_id, status)
  WHERE assignee_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_ticket_report_team_status
  ON ticket_report_projection (assigned_team_id, status)
  WHERE assigned_team_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_ticket_report_closed_at
  ON ticket_report_projection (closed_at)
  WHERE closed_at IS NOT NULL;

CREATE TABLE IF NOT EXISTS ticket_status_daily_projection (
  report_date DATE NOT NULL,
  status VARCHAR(40) NOT NULL,
  ticket_count BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (report_date, status),
  CONSTRAINT ticket_status_daily_status_check CHECK (
    status IN ('NEW', 'IN_PROGRESS', 'WAITING_FOR_CUSTOMER', 'RESOLVED', 'CLOSED')
  ),
  CONSTRAINT ticket_status_daily_count_check CHECK (ticket_count >= 0)
);

CREATE TABLE IF NOT EXISTS agent_performance_daily_projection (
  report_date DATE NOT NULL,
  agent_id UUID NOT NULL,
  assigned_count BIGINT NOT NULL DEFAULT 0,
  resolved_count BIGINT NOT NULL DEFAULT 0,
  breached_sla_count BIGINT NOT NULL DEFAULT 0,
  avg_resolution_minutes NUMERIC(12, 2),
  PRIMARY KEY (report_date, agent_id),
  CONSTRAINT agent_performance_assigned_count_check CHECK (assigned_count >= 0),
  CONSTRAINT agent_performance_resolved_count_check CHECK (resolved_count >= 0),
  CONSTRAINT agent_performance_breach_count_check CHECK (breached_sla_count >= 0),
  CONSTRAINT agent_performance_avg_resolution_check CHECK (
    avg_resolution_minutes IS NULL OR avg_resolution_minutes >= 0
  )
);

CREATE INDEX IF NOT EXISTS idx_agent_performance_agent_date
  ON agent_performance_daily_projection (agent_id, report_date DESC);

CREATE TABLE IF NOT EXISTS sla_compliance_daily_projection (
  report_date DATE NOT NULL,
  priority VARCHAR(20) NOT NULL,
  met_count BIGINT NOT NULL DEFAULT 0,
  breached_count BIGINT NOT NULL DEFAULT 0,
  at_risk_count BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (report_date, priority),
  CONSTRAINT sla_compliance_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
  CONSTRAINT sla_compliance_met_count_check CHECK (met_count >= 0),
  CONSTRAINT sla_compliance_breached_count_check CHECK (breached_count >= 0),
  CONSTRAINT sla_compliance_at_risk_count_check CHECK (at_risk_count >= 0)
);
