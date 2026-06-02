CREATE TABLE IF NOT EXISTS ticket_topics (
  id UUID PRIMARY KEY,
  code VARCHAR(80) NOT NULL UNIQUE,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(500) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ticket_topics_active ON ticket_topics (active);

CREATE TABLE IF NOT EXISTS ticket_routing_rules (
  id UUID PRIMARY KEY,
  topic_id UUID NOT NULL REFERENCES ticket_topics(id),
  department_id UUID NOT NULL REFERENCES departments(id),
  team_id UUID NOT NULL REFERENCES support_teams(id),
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ticket_routing_rules_topic_unique UNIQUE (topic_id)
);

CREATE INDEX IF NOT EXISTS idx_ticket_routing_rules_topic ON ticket_routing_rules (topic_id);
CREATE INDEX IF NOT EXISTS idx_ticket_routing_rules_team ON ticket_routing_rules (team_id);
CREATE INDEX IF NOT EXISTS idx_ticket_routing_rules_active ON ticket_routing_rules (active);

ALTER TABLE tickets
  ADD COLUMN IF NOT EXISTS topic_id UUID REFERENCES ticket_topics(id),
  ADD COLUMN IF NOT EXISTS routed_department_id UUID REFERENCES departments(id);

CREATE INDEX IF NOT EXISTS idx_tickets_topic ON tickets (topic_id);
CREATE INDEX IF NOT EXISTS idx_tickets_routed_department ON tickets (routed_department_id);

INSERT INTO ticket_topics (id, code, name, description, active)
VALUES
  ('60000000-0000-0000-0000-000000000001', 'PASSWORD_RESET', 'Password Reset', 'Password reset or account login access problems.', true),
  ('60000000-0000-0000-0000-000000000002', 'PERMISSION_REQUEST', 'Permission Request', 'Role, permission, or access scope requests.', true),
  ('60000000-0000-0000-0000-000000000003', 'WEB_PORTAL_BUG', 'Web Portal Bug', 'Web portal defects and customer-facing web application errors.', true),
  ('60000000-0000-0000-0000-000000000004', 'CORE_SYSTEM_ERROR', 'Core System Error', 'Core platform service errors and backend process failures.', true),
  ('60000000-0000-0000-0000-000000000005', 'NETWORK_CONNECTIVITY', 'Network Connectivity', 'Connection, VPN, DNS, or network reachability issues.', true),
  ('60000000-0000-0000-0000-000000000006', 'SERVER_PLATFORM', 'Server Platform', 'Server, runtime, deployment, or platform stability issues.', true),
  ('60000000-0000-0000-0000-000000000007', 'INVOICE_ISSUE', 'Invoice Issue', 'Invoice, statement, or billing document problems.', true),
  ('60000000-0000-0000-0000-000000000008', 'PAYMENT_FAILURE', 'Payment Failure', 'Failed, delayed, duplicate, or disputed payment problems.', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO ticket_routing_rules (id, topic_id, department_id, team_id, active)
VALUES
  ('70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', true),
  ('70000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', true),
  ('70000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', true),
  ('70000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004', true),
  ('70000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005', true),
  ('70000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000006', true),
  ('70000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000007', true),
  ('70000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000008', true)
ON CONFLICT (topic_id) DO NOTHING;
