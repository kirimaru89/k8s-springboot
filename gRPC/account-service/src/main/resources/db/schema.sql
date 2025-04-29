-- Drop table if exists
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS account_reservations;

-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance DOUBLE PRECISION NOT NULL
    -- created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on user_id for faster lookups
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Add comment to table
COMMENT ON TABLE accounts IS 'Stores user account information';

-- Add comments to columns
COMMENT ON COLUMN accounts.id IS 'Primary key of the account';
COMMENT ON COLUMN accounts.user_id IS 'Foreign key referencing the user';
COMMENT ON COLUMN accounts.balance IS 'Current balance of the account';
-- COMMENT ON COLUMN accounts.created_at IS 'Timestamp when the account was created';
-- COMMENT ON COLUMN accounts.updated_at IS 'Timestamp when the account was last updated';


CREATE TABLE account_reservations (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT                                  NOT NULL,
    account_id     BIGINT                                  NOT NULL,
    amount         DOUBLE PRECISION                        NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL
);


CREATE INDEX idx_account_reservations_account_id ON account_reservations(account_id);
