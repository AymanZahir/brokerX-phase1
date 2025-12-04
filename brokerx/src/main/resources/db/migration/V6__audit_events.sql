CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    trace_id VARCHAR(64) NOT NULL,
    category VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    actor_id UUID,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_event_trace ON audit_event(trace_id);
CREATE INDEX idx_audit_event_category_created ON audit_event(category, created_at DESC);
