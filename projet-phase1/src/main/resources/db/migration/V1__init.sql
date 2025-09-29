CREATE TABLE account (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    mfa_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    available_balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_wallet_account UNIQUE (account_id)
);

CREATE TABLE tx_journal (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallet(id) ON DELETE CASCADE,
    type VARCHAR(16) NOT NULL,
    amount NUMERIC(18,2) NOT NULL CHECK (amount >= 0),
    status VARCHAR(16) NOT NULL,
    idempotency_key VARCHAR(128) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_journal_wallet ON tx_journal(wallet_id);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    side VARCHAR(8) NOT NULL,
    type VARCHAR(8) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    qty BIGINT NOT NULL CHECK (qty > 0),
    limit_price NUMERIC(18,4),
    status VARCHAR(32) NOT NULL,
    client_order_id VARCHAR(64) UNIQUE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_account ON orders(account_id);
