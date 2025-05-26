CREATE TABLE IF NOT EXISTS business (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, device_name VARCHAR(512) NOT NULL, created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL);
ALTER TABLE business ADD CONSTRAINT business_user_id_unique UNIQUE (user_id);
CREATE TABLE IF NOT EXISTS business_dashboard (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, business_id BIGINT NOT NULL, created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL, CONSTRAINT fk_business_dashboard_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
ALTER TABLE business_dashboard ADD CONSTRAINT business_dashboard_user_id_unique UNIQUE (user_id);
CREATE INDEX business_dashboard_business_id ON business_dashboard (business_id);
