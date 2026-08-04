CREATE TABLE IF NOT EXISTS business_working_hours (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL, business_id BINARY(16) NOT NULL, day_of_week TINYINT NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, CONSTRAINT fk_business_working_hours_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX business_working_hours_business_id ON business_working_hours (business_id);
CREATE TABLE IF NOT EXISTS business_day_offs (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL, business_id BINARY(16) NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, CONSTRAINT fk_business_day_offs_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX business_day_offs_business_id ON business_day_offs (business_id);
CREATE INDEX business_day_offs_business_id_start_date ON business_day_offs (business_id, start_date);
ALTER TABLE business ADD working_days TINYINT DEFAULT 62 NOT NULL;
