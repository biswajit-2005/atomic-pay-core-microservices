-- Ledger Schema Migration (V1)

CREATE TABLE IF NOT EXISTS ledger_accounts (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    entry_type VARCHAR(10) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    sequence_no INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_entry_amount CHECK (amount > 0),
    UNIQUE(transaction_id, sequence_no)
);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_tx ON ledger_entries(transaction_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_account ON ledger_entries(ledger_account_id, created_at);
