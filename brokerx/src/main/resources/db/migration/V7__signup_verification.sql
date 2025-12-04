ALTER TABLE account
    ADD COLUMN full_name VARCHAR(255),
    ADD COLUMN phone VARCHAR(32),
    ADD COLUMN address_line TEXT,
    ADD COLUMN country VARCHAR(64),
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN kyc_verified_at TIMESTAMPTZ;

UPDATE account
SET full_name = 'Seed User',
    phone = '+15550000001',
    address_line = 'Seed Street 123, Montreal, QC',
    country = 'CA',
    date_of_birth = DATE '1990-01-01',
    kyc_verified_at = NOW()
WHERE id = '11111111-1111-1111-1111-111111111111';

ALTER TABLE account
    ALTER COLUMN full_name SET NOT NULL,
    ALTER COLUMN phone SET NOT NULL,
    ADD CONSTRAINT uq_account_phone UNIQUE (phone);

CREATE TABLE verification_request (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    otp VARCHAR(16) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    channel VARCHAR(32) NOT NULL DEFAULT 'EMAIL'
);

CREATE UNIQUE INDEX uq_verification_account_pending
    ON verification_request(account_id)
    WHERE status = 'PENDING';

CREATE UNIQUE INDEX uq_verification_otp ON verification_request(otp);
