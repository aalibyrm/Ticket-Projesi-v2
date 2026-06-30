ALTER TABLE ticket_routing_rules
  ADD COLUMN IF NOT EXISTS routing_order INTEGER NOT NULL DEFAULT 0;

ALTER TABLE ticket_routing_rules
  DROP CONSTRAINT IF EXISTS ticket_routing_rules_topic_unique;

CREATE UNIQUE INDEX IF NOT EXISTS idx_ticket_routing_rules_topic_team_unique
  ON ticket_routing_rules (topic_id, team_id);

UPDATE ticket_routing_rules
SET routing_order = 0
WHERE routing_order IS NULL;

INSERT INTO ticket_routing_rules (id, topic_id, department_id, team_id, routing_order, active)
VALUES (
  '70000000-0000-0000-0000-000000000009',
  '60000000-0000-0000-0000-000000000008',
  '10000000-0000-0000-0000-000000000004',
  '20000000-0000-0000-0000-000000000009',
  1,
  true
)
ON CONFLICT (topic_id, team_id) DO UPDATE
SET routing_order = EXCLUDED.routing_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS ticket_routing_cursors (
  topic_id UUID PRIMARY KEY REFERENCES ticket_topics(id),
  next_route_index INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO ticket_routing_cursors (topic_id, next_route_index)
SELECT topic_id, 0
FROM ticket_routing_rules
WHERE active = true
GROUP BY topic_id
ON CONFLICT (topic_id) DO NOTHING;
