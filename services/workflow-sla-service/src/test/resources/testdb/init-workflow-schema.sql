CREATE SCHEMA IF NOT EXISTS workflow_schema AUTHORIZATION workflow_app;
GRANT USAGE, CREATE ON SCHEMA workflow_schema TO workflow_app;
ALTER ROLE workflow_app SET search_path TO workflow_schema;
