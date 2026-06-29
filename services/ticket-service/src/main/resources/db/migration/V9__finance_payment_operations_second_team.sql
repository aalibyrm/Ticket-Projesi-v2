UPDATE support_teams
SET code = 'PAYMENT_OPERATIONS_1',
    name = 'Payment Operations 1',
    lead_actor_id = '30000000-0000-0000-0000-000000000008'
WHERE id = '20000000-0000-0000-0000-000000000008';

INSERT INTO support_teams (id, department_id, code, name, lead_actor_id, active)
VALUES (
  '20000000-0000-0000-0000-000000000009',
  '10000000-0000-0000-0000-000000000004',
  'PAYMENT_OPERATIONS_2',
  'Payment Operations 2',
  '30000000-0000-0000-0000-000000000009',
  true
)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    lead_actor_id = EXCLUDED.lead_actor_id,
    active = EXCLUDED.active;

INSERT INTO team_members (id, team_id, actor_id, team_lead, active)
VALUES
  (
    '50000000-0000-0000-0000-000000000017',
    '20000000-0000-0000-0000-000000000009',
    '30000000-0000-0000-0000-000000000009',
    true,
    true
  ),
  (
    '50000000-0000-0000-0000-000000000018',
    '20000000-0000-0000-0000-000000000009',
    '40000000-0000-0000-0000-000000000009',
    false,
    true
  )
ON CONFLICT (team_id, actor_id) DO UPDATE
SET team_lead = EXCLUDED.team_lead,
    active = EXCLUDED.active;
