#!/usr/bin/env bash
set -euo pipefail

psql \
  --username "${POSTGRES_USER}" \
  --dbname "${POSTGRES_DB}" \
  -v ticket_password="${TICKET_DB_PASSWORD}" \
  -v workflow_password="${WORKFLOW_DB_PASSWORD}" \
  -v file_password="${FILE_DB_PASSWORD}" \
  -v notification_password="${NOTIFICATION_DB_PASSWORD}" \
  -v reporting_password="${REPORTING_DB_PASSWORD}" <<'EOSQL'
SELECT format('CREATE ROLE ticket_app LOGIN PASSWORD %L', :'ticket_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'ticket_app')\gexec

SELECT format('CREATE ROLE workflow_app LOGIN PASSWORD %L', :'workflow_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'workflow_app')\gexec

SELECT format('CREATE ROLE file_app LOGIN PASSWORD %L', :'file_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'file_app')\gexec

SELECT format('CREATE ROLE notification_app LOGIN PASSWORD %L', :'notification_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'notification_app')\gexec

SELECT format('CREATE ROLE reporting_app LOGIN PASSWORD %L', :'reporting_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'reporting_app')\gexec

CREATE SCHEMA IF NOT EXISTS ticket_schema AUTHORIZATION ticket_app;
CREATE SCHEMA IF NOT EXISTS workflow_schema AUTHORIZATION workflow_app;
CREATE SCHEMA IF NOT EXISTS file_schema AUTHORIZATION file_app;
CREATE SCHEMA IF NOT EXISTS notification_schema AUTHORIZATION notification_app;
CREATE SCHEMA IF NOT EXISTS reporting_schema AUTHORIZATION reporting_app;

GRANT USAGE, CREATE ON SCHEMA ticket_schema TO ticket_app;
GRANT USAGE, CREATE ON SCHEMA workflow_schema TO workflow_app;
GRANT USAGE, CREATE ON SCHEMA file_schema TO file_app;
GRANT USAGE, CREATE ON SCHEMA notification_schema TO notification_app;
GRANT USAGE, CREATE ON SCHEMA reporting_schema TO reporting_app;
EOSQL
