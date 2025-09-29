CREATE TABLE account_credentials (
    account_id UUID PRIMARY KEY REFERENCES account(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    mfa_secret VARCHAR(32),
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ
);

CREATE TABLE account_session (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    last_ip VARCHAR(64),
    user_agent VARCHAR(255)
);

CREATE INDEX idx_session_account ON account_session(account_id);
