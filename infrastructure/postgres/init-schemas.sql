-- Initialize PostgreSQL Schemas for Atomic-pay Distributed Banking System

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS transactions;
CREATE SCHEMA IF NOT EXISTS ledger;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Grant permissions if necessary
GRANT ALL ON SCHEMA auth TO postgres;
GRANT ALL ON SCHEMA accounts TO postgres;
GRANT ALL ON SCHEMA transactions TO postgres;
GRANT ALL ON SCHEMA ledger TO postgres;
GRANT ALL ON SCHEMA audit TO postgres;
GRANT ALL ON SCHEMA notifications TO postgres;
GRANT ALL ON SCHEMA analytics TO postgres;
