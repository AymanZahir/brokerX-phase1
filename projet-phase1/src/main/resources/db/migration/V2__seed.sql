INSERT INTO account (id, email, status, mfa_enabled)
VALUES ('11111111-1111-1111-1111-111111111111', 'seed@brokerx.dev', 'ACTIVE', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO wallet (id, account_id, available_balance)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    1000.00
)
ON CONFLICT (id) DO NOTHING;
