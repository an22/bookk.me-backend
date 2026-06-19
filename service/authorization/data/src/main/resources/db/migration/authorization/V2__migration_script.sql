CREATE TABLE IF NOT EXISTS signing_key (id BINARY(16) PRIMARY KEY, public_key text NOT NULL, private_key text NOT NULL, status INT NOT NULL, created_at TIMESTAMP(6) NOT NULL, retired_at TIMESTAMP(6) NULL);
CREATE INDEX signing_key_status ON signing_key (status);
ALTER TABLE auth_device ADD refresh_token_expires_at TIMESTAMP(6) NULL;
