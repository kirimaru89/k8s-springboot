-- Drop table if exists
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS account_reservations;

-- Create accounts table
CREATE TABLE accounts (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL,
      balance DOUBLE PRECISION NOT NULL
);

-- Create index on user_id for faster lookups
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

CREATE TABLE account_reservations (
      id              BIGSERIAL PRIMARY KEY,
      transaction_id BIGINT                                  NOT NULL,
      account_id     BIGINT                                  NOT NULL,
      amount         DOUBLE PRECISION                        NOT NULL,
      type VARCHAR(10) NOT NULL,
      status VARCHAR(10) NOT NULL
);


CREATE INDEX idx_account_reservations_account_id ON account_reservations(account_id);
