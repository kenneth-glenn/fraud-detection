-- Create yearly partitions for history table
CREATE TABLE transaction_history_2025 PARTITION OF transaction_history
FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE transaction_history_default PARTITION OF transaction_history DEFAULT;
