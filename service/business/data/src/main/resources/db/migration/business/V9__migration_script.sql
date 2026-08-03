CREATE TABLE IF NOT EXISTS employee (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL, business_id BINARY(16) NOT NULL, `name` VARCHAR(512) NOT NULL, lastname VARCHAR(512) NOT NULL, phone VARCHAR(512) NULL, email VARCHAR(512) NULL, user_id BINARY(16) NOT NULL, CONSTRAINT fk_employee_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX employee_business_id ON employee (business_id);
CREATE INDEX employee_phone ON employee (phone);
CREATE INDEX employee_email ON employee (email);
CREATE INDEX employee_user_id ON employee (user_id);
CREATE TABLE IF NOT EXISTS employee_can_provide_service (id BINARY(16) PRIMARY KEY, employee_id BINARY(16) NOT NULL, service_id BINARY(16) NOT NULL, business_id BINARY(16) NOT NULL, CONSTRAINT fk_employee_can_provide_service_employee_id__id FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_employee_can_provide_service_service_id__id FOREIGN KEY (service_id) REFERENCES service(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_employee_can_provide_service_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX employee_can_provide_service_service_id ON employee_can_provide_service (service_id);
CREATE INDEX employee_can_provide_service_business_id ON employee_can_provide_service (business_id);
ALTER TABLE employee_can_provide_service ADD CONSTRAINT employee_can_provide_service_employee_id_service_id_unique UNIQUE (employee_id, service_id);
