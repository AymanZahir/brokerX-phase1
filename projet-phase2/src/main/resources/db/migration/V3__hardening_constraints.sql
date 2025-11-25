-- Strengthen constraints & add helpful indexes

-- 1) Account: constrain status values and default MFA
ALTER TABLE account
  ALTER COLUMN mfa_enabled SET DEFAULT FALSE;

ALTER TABLE account
  ADD CONSTRAINT chk_account_status
  CHECK (status IN ('PENDING','ACTIVE','SUSPENDED','REJECTED'));

-- 3) Tx journal: amount strictly positive, enumerated type & composite index
ALTER TABLE tx_journal
  DROP CONSTRAINT IF EXISTS tx_journal_amount_check,
  ADD CONSTRAINT chk_tx_amount_positive CHECK (amount > 0);

ALTER TABLE tx_journal
  ADD CONSTRAINT chk_tx_type
  CHECK (type IN ('DEPOSIT','WITHDRAW'));

ALTER TABLE tx_journal
  ADD CONSTRAINT chk_tx_status
  CHECK (status IN ('PENDING','SETTLED','FAILED'));

DROP INDEX IF EXISTS idx_tx_journal_wallet;
CREATE INDEX idx_tx_wallet_status ON tx_journal(wallet_id, status);

-- 4) Orders: constrain enums, enforce MARKET/LIMIT rule, add composite index
ALTER TABLE orders
  ADD CONSTRAINT chk_order_side
  CHECK (side IN ('BUY','SELL'));

ALTER TABLE orders
  ADD CONSTRAINT chk_order_type
  CHECK (type IN ('MARKET','LIMIT'));

ALTER TABLE orders
  ADD CONSTRAINT chk_order_price_by_type
  CHECK (
    (type = 'MARKET' AND limit_price IS NULL)
    OR
    (type = 'LIMIT'  AND limit_price IS NOT NULL AND limit_price > 0)
  );

ALTER TABLE orders
  ADD CONSTRAINT chk_order_status
  CHECK (status IN ('NEW','WORKING','PARTIALLY_FILLED','FILLED','REJECTED','CANCELLED'));

CREATE INDEX IF NOT EXISTS idx_orders_acct_created ON orders(account_id, created_at);
