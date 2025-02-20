-- Transaction history trigger function
CREATE
OR
REPLACE FUNCTION archive_transaction()
RETURNS TRIGGER AS $$
BEGIN IF NEW.is_current = FALSE THEN
INSERT INTO transaction_history
VALUES (OLD.transaction_id, OLD.customer_name, OLD.ip_address,
        OLD.customer_city, OLD.customer_state, OLD.card_last4,
        OLD.name_on_card, OLD.purchase_amount, OLD.merchant_name,
        OLD.merchant_city, OLD.merchant_state, OLD.purchased_item_count,
        OLD.valid_from, NEW.valid_from);
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach trigger to transaction table
CREATE TRIGGER transaction_history_trigger
    BEFORE UPDATE
    ON transaction_current
    FOR EACH ROW EXECUTE FUNCTION archive_transaction();
