-- Audit Schema Migration (V1)

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    event_id UUID,
    actor_user_id UUID,
    transaction_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80),
    entity_id UUID,
    metadata JSONB,
    ip_address INET,
    user_agent TEXT,
    correlation_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS processed_events (
    consumer_name VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY(consumer_name, event_id)
);

CREATE INDEX IF NOT EXISTS idx_audit_transaction_id ON audit_logs(transaction_id);
CREATE INDEX IF NOT EXISTS idx_audit_actor_created ON audit_logs(actor_user_id, created_at);
