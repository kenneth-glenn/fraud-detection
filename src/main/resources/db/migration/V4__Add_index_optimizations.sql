-- BRIN index for temporal queries
CREATE INDEX idx_transaction_current_time ON transaction_current USING BRIN (valid_from);

-- GIN index for fraud signal details
CREATE INDEX idx_fraud_details ON fraud_signal USING GIN (details);
