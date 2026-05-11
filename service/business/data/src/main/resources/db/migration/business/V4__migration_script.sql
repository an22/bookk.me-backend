CREATE TABLE IF NOT EXISTS business_permissions (id BINARY(16) PRIMARY KEY, user_id BINARY(16) NOT NULL, business_id BINARY(16) NOT NULL, permission INT NOT NULL, CONSTRAINT fk_business_permissions_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
ALTER TABLE business_permissions ADD CONSTRAINT business_permissions_user_id_business_id_unique UNIQUE (user_id, business_id);
CREATE TABLE IF NOT EXISTS service_group (id BINARY(16) PRIMARY KEY, business_id BINARY(16) NOT NULL, `name` VARCHAR(512) NOT NULL, CONSTRAINT fk_service_group_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX service_group_business_id ON service_group (business_id);
ALTER TABLE service_group ADD CONSTRAINT service_group_business_id_name_unique UNIQUE (business_id, `name`);
CREATE TABLE IF NOT EXISTS service (id BINARY(16) PRIMARY KEY, business_id BINARY(16) NOT NULL, group_id BINARY(16) NOT NULL, `name` VARCHAR(512) NOT NULL, duration INT NOT NULL, price_currency VARCHAR(3) NOT NULL, price_unscaled BIGINT NOT NULL, price_scale INT NOT NULL, CONSTRAINT fk_service_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_service_group_id__id FOREIGN KEY (group_id) REFERENCES service_group(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX service_business_id ON service (business_id);
CREATE INDEX service_group_id ON service (group_id);
ALTER TABLE service ADD CONSTRAINT service_business_id_group_id_name_unique UNIQUE (business_id, group_id, `name`);
