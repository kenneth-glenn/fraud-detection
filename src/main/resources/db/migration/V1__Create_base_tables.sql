-- Core transaction table with SCD4 pattern
CREATE TABLE transaction_current
(
    transaction_id       UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    customer_name        TEXT           NOT NULL,
    ip_address           INET           NOT NULL,
    customer_city        TEXT           NOT NULL,
    customer_state       CHAR(2)        NOT NULL,
    card_last4           CHAR(4)        NOT NULL,
    name_on_card         TEXT           NOT NULL,
    purchase_amount      NUMERIC(19, 2) NOT NULL,
    merchant_name        TEXT           NOT NULL,
    merchant_city        TEXT           NOT NULL,
    merchant_state       CHAR(2)        NOT NULL,
    purchased_item_count INTEGER        NOT NULL,
    valid_from           TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to             TIMESTAMPTZ,
    is_current           BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_transaction_current ON transaction_current (is_current);
CREATE INDEX idx_transaction_customer ON transaction_current (customer_name);

-- Historical transaction table
CREATE TABLE transaction_history
(
    transaction_id       UUID           NOT NULL,
    customer_name        TEXT           NOT NULL,
    ip_address           INET           NOT NULL,
    customer_city        TEXT           NOT NULL,
    customer_state       CHAR(2)        NOT NULL,
    card_last4           CHAR(4)        NOT NULL,
    name_on_card         TEXT           NOT NULL,
    purchase_amount      NUMERIC(19, 2) NOT NULL,
    merchant_name        TEXT           NOT NULL,
    merchant_city        TEXT           NOT NULL,
    merchant_state       CHAR(2)        NOT NULL,
    purchased_item_count INTEGER        NOT NULL,
    valid_from           TIMESTAMPTZ    NOT NULL,
    valid_to             TIMESTAMPTZ    NOT NULL
) PARTITION BY RANGE (valid_from);

CREATE INDEX idx_transaction_history ON transaction_history (transaction_id, valid_from);

-- Fraud signals with temporal tracking
CREATE TABLE fraud_signal
(
    signal_id       BIGSERIAL PRIMARY KEY,
    transaction_id  UUID        NOT NULL REFERENCES transaction_current (transaction_id),
    signal_type     TEXT        NOT NULL CHECK (signal_type IN ('location', 'ipAddress', 'transaction', 'cardDetails')),
    potential_fraud BOOLEAN     NOT NULL,
    details         TEXT[]      NOT NULL
);

CREATE INDEX idx_fraud_signal_temporal ON fraud_signal (transaction_id);
