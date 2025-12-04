INSERT INTO account_credentials (account_id, password_hash, mfa_secret)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'password123',
    '123456'
)
ON CONFLICT (account_id) DO NOTHING;

UPDATE account
SET mfa_enabled = TRUE
WHERE id = '11111111-1111-1111-1111-111111111111';
