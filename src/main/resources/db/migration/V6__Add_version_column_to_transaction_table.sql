ALTER TABLE transaction_current ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE transaction_history ADD COLUMN version BIGINT DEFAULT 0;
