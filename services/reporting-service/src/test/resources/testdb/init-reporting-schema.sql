CREATE SCHEMA IF NOT EXISTS reporting_schema AUTHORIZATION reporting_app;
GRANT USAGE, CREATE ON SCHEMA reporting_schema TO reporting_app;
ALTER ROLE reporting_app SET search_path TO reporting_schema;
