-- Drop table if exists
DROP TABLE IF EXISTS transactions;

-- Create transactions table
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT NOT NULL,
                              amount DOUBLE PRECISION NOT NULL,
                              type VARCHAR(10) NOT NULL,
                              status VARCHAR(10) NOT NULL
);

-- Create index on account_id for faster lookups
CREATE INDEX idx_transactions_account_id ON transactions(account_id);

-- Add comment to table
COMMENT ON TABLE transactions IS 'Stores transaction information';

-- Add comments to columns
COMMENT ON COLUMN transactions.id IS 'Primary key of the transaction';
COMMENT ON COLUMN transactions.account_id IS 'Foreign key referencing the account';
COMMENT ON COLUMN transactions.amount IS 'Amount of the transaction';
COMMENT ON COLUMN transactions.type IS 'Type of the transaction';
COMMENT ON COLUMN transactions.status IS 'Status of the transaction';
