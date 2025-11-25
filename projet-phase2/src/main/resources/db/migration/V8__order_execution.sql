CREATE TABLE execution (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    counter_order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
    account_id UUID NOT NULL,
    counter_account_id UUID,
    symbol VARCHAR(32) NOT NULL,
    qty BIGINT NOT NULL CHECK (qty > 0),
    price NUMERIC(18,4) NOT NULL CHECK (price > 0),
    side VARCHAR(8) NOT NULL,
    execution_time TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_execution_order ON execution(order_id);
CREATE INDEX idx_execution_symbol ON execution(symbol);
