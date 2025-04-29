-- Drop table if exists
DROP TABLE IF EXISTS fraud_detections;

CREATE TABLE fraud_detections (
      id              BIGSERIAL PRIMARY KEY,
      transaction_id BIGINT                                  NOT NULL,
      is_fraudulent  BOOLEAN                                 NOT NULL,
      reason         VARCHAR(255)
);

