-- Transaction Schema Migration (V1)

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    sender_account_id UUID NOT NULL,
    receiver_account_id UUID NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    failure_code VARCHAR(80),
    failure_reason TEXT,
    correlation_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_transaction_idempotency UNIQUE(sender_account_id, idempotency_key),
    CONSTRAINT chk_amount_positive CHECK(amount > 0),
    CONSTRAINT chk_different_accounts CHECK(sender_account_id <> receiver_account_id)
);

CREATE TABLE IF NOT EXISTS transaction_steps (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    step_order INT NOT NULL,
    step_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_code VARCHAR(80),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE(transaction_id, step_order)
);

CREATE TABLE IF NOT EXISTS idempotency_records (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    transaction_id UUID REFERENCES transactions(id),
    response_status INT,
    response_body JSONB,
    status VARCHAR(30) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE(user_id, idempotency_key)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS reconciliation_runs (
    id UUID PRIMARY KEY,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL,
    total_transactions INT DEFAULT 0,
    mismatches INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reconciliation_issues (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES reconciliation_runs(id),
    transaction_id UUID REFERENCES transactions(id),
    account_id UUID,
    issue_type VARCHAR(80) NOT NULL,
    expected_value NUMERIC(19,4),
    actual_value NUMERIC(19,4),
    status VARCHAR(30) NOT NULL,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tx_sender_created ON transactions(sender_account_id, created_at);
CREATE INDEX IF NOT EXISTS idx_tx_receiver_created ON transactions(receiver_account_id, created_at);
CREATE INDEX IF NOT EXISTS idx_tx_status_created ON transactions(status, created_at);
CREATE INDEX IF NOT EXISTS idx_tx_correlation_id ON transactions(correlation_id);
CREATE INDEX IF NOT EXISTS idx_tx_steps_transaction_id ON transaction_steps(transaction_id);
CREATE INDEX IF NOT EXISTS idx_outbox_status_next_attempt ON outbox_events(status, next_attempt_at);
