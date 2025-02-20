-- Immutable audit log table
CREATE TABLE audit_log
(
    log_id BIGSERIAL PRIMARY KEY,
    table_name  TEXT NOT NULL,
    record_id UUID NOT NULL,
    operation   TEXT NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    old_state JSONB,
    new_state JSONB,
    executed_by TEXT NOT NULL,
    executed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Row-level security
ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;

CREATE
POLICY audit_log_read_policy
ON audit_log
FOR
SELECT
    USING
(current_user = executed_by OR pg_has_role(current_user, 'audit_admin', 'MEMBER'));
