CREATE TABLE IF NOT EXISTS notification_settings (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL, user_id BINARY(16) NOT NULL, appointment_enabled BOOLEAN NOT NULL);
ALTER TABLE notification_settings ADD CONSTRAINT notification_settings_user_id_unique UNIQUE (user_id);
