CREATE TABLE IF NOT EXISTS client (id BINARY(16) PRIMARY KEY, business_id BINARY(16) NOT NULL, `name` VARCHAR(512) NOT NULL, lastname VARCHAR(512) NOT NULL, phone VARCHAR(512) NOT NULL, user_id BINARY(16) NULL);
CREATE INDEX client_business_id ON client (business_id);
CREATE INDEX client_user_id ON client (user_id);
ALTER TABLE business MODIFY COLUMN description VARCHAR(1024) NOT NULL;
