CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    type VARCHAR(128) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE NULL,
    error_message TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created_at ON outbox_events(status, created_at);
