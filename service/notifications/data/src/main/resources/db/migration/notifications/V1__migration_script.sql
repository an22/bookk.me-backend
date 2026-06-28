CREATE TABLE IF NOT EXISTS device (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL, auth_id BINARY(16) NOT NULL, device_uuid BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, notification_token text NULL);
ALTER TABLE device ADD CONSTRAINT device_device_uuid_unique UNIQUE (device_uuid);
ALTER TABLE device ADD CONSTRAINT device_auth_id_device_uuid_user_id_unique UNIQUE (auth_id, device_uuid, user_id);
