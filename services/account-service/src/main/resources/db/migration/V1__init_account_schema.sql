-- Account Schema Migration (V1)

CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    account_number VARCHAR(32) UNIQUE NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    account_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_account_balance_positive CHECK (current_balance >= 0),
    CONSTRAINT chk_account_currency CHECK (currency = 'INR')
);

CREATE TABLE IF NOT EXISTS beneficiaries (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    beneficiary_account_id UUID NOT NULL REFERENCES accounts(id),
    nickname VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(owner_user_id, beneficiary_account_id)
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
