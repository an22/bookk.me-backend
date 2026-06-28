ALTER TABLE device ADD device_uuid BINARY(16) NOT NULL;
ALTER TABLE device ADD CONSTRAINT device_device_uuid_unique UNIQUE (device_uuid);
ALTER TABLE device ADD CONSTRAINT device_auth_id_device_uuid_user_id_unique UNIQUE (auth_id, device_uuid, user_id);
ALTER TABLE device DROP COLUMN device_id;
ALTER TABLE device DROP INDEX device_auth_id_unique;
ALTER TABLE device DROP INDEX device_device_id_unique;
ALTER TABLE device DROP INDEX device_user_id_unique;
