CREATE TABLE IF NOT EXISTS ticket_conversation_reads (
  id UUID PRIMARY KEY,
  ticket_id UUID NOT NULL REFERENCES tickets(id),
  actor_id UUID NOT NULL,
  scope VARCHAR(40) NOT NULL,
  last_read_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ticket_conversation_reads_scope_check CHECK (scope IN ('CUSTOMER_EXTERNAL', 'SUPPORT_ALL')),
  CONSTRAINT uk_ticket_conversation_reads_actor_scope UNIQUE (ticket_id, actor_id, scope)
);

CREATE INDEX IF NOT EXISTS idx_ticket_conversation_reads_actor_scope
  ON ticket_conversation_reads (actor_id, scope, updated_at DESC);
