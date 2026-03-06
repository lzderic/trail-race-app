-- Reset Flyway migration history for both databases.
-- Run this ONCE after switching from filesystem: to classpath: Flyway locations.
--
-- Usage (connect to each database and run):
--   psql -h localhost -U trailrace -d trailrace_command -f scripts/reset_flyway_history.sql
--   psql -h localhost -U trailrace -d trailrace_query   -f scripts/reset_flyway_history.sql
--
-- Or via Docker:
--   docker exec -i trailrace-postgres psql -U trailrace -d trailrace_command < scripts/reset_flyway_history.sql
--   docker exec -i trailrace-postgres psql -U trailrace -d trailrace_query   < scripts/reset_flyway_history.sql

DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS flyway_schema_history_query CASCADE;

-- Also drop the application tables so Flyway can recreate them cleanly
DROP TABLE IF EXISTS applications CASCADE;
DROP TABLE IF EXISTS races CASCADE;
DROP TABLE IF EXISTS users CASCADE;

