CREATE TABLE notification (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    order_id UUID,
    type VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delivered_at TIMESTAMPTZ
);

CREATE INDEX idx_notification_account ON notification(account_id);
CREATE INDEX idx_notification_order ON notification(order_id);
