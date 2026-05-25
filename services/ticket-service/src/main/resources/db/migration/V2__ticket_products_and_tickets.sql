CREATE SEQUENCE IF NOT EXISTS ticket_number_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY,
  code VARCHAR(80) NOT NULL UNIQUE,
  name VARCHAR(160) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tickets (
  id UUID PRIMARY KEY,
  ticket_number VARCHAR(32) NOT NULL UNIQUE,
  customer_id UUID NOT NULL,
  product_id UUID NOT NULL REFERENCES products(id),
  summary VARCHAR(180) NOT NULL,
  description VARCHAR(5000) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  status VARCHAR(40) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT tickets_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
  CONSTRAINT tickets_status_check CHECK (status IN ('NEW', 'IN_PROGRESS', 'WAITING_FOR_CUSTOMER', 'RESOLVED', 'CLOSED'))
);

CREATE INDEX IF NOT EXISTS idx_tickets_customer_created_at ON tickets (customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets (status);
CREATE INDEX IF NOT EXISTS idx_tickets_product ON tickets (product_id);

INSERT INTO products (id, code, name, active)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'WEB_PORTAL', 'Web Portal', true),
  ('22222222-2222-2222-2222-222222222222', 'MOBILE_APP', 'Mobile App', true),
  ('33333333-3333-3333-3333-333333333333', 'CORE_PLATFORM', 'Core Platform', true)
ON CONFLICT (code) DO NOTHING;

