CREATE SCHEMA IF NOT EXISTS notification_schema AUTHORIZATION notification_app;
GRANT USAGE, CREATE ON SCHEMA notification_schema TO notification_app;
ALTER ROLE notification_app SET search_path TO notification_schema;
