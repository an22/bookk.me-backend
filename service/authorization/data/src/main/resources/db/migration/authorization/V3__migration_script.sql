ALTER TABLE auth_device ADD refresh_token_hash VARCHAR(64) NULL;
ALTER TABLE auth_device ADD previous_refresh_token_id BINARY(16) NULL;
ALTER TABLE auth_device ADD previous_refresh_token_hash VARCHAR(64) NULL;
