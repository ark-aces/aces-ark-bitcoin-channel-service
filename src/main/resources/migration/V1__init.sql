CREATE TABLE contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(20),
  recipient_btc_address VARCHAR(255),
  deposit_ark_address VARCHAR(255),
  deposit_ark_address_passphrase VARCHAR(255),
  subscription_id VARCHAR(255),
  created_at TIMESTAMP
);

CREATE TABLE transfers (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  contract_pid BIGINT NOT NULL,
  status VARCHAR(255),
  ark_transaction_id VARCHAR(255),
  ark_amount DECIMAL(8,5),
  ark_to_btc_rate DECIMAL(8,5),
  ark_flat_fee DECIMAL(8,5),
  ark_percent_fee DECIMAL(8,5),
  ark_total_fee DECIMAL(8,5),
  btc_send_amount DECIMAL(8,5),
  btc_transaction_id VARCHAR(255),
  needs_btc_confirmation BOOLEAN,
  btc_confirmation_subscription_id VARCHAR(255),
  needs_ark_return BOOLEAN,
  return_ark_transaction_id VARHCHAR(255)
);
ALTER TABLE transfers ADD CONSTRAINT FOREIGN KEY (contract_pid) REFERENCES contracts (pid);
