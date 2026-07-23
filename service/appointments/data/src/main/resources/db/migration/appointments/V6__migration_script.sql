ALTER TABLE appointment ADD employee_id BINARY(16) NOT NULL;
ALTER TABLE appointment ADD employee_name VARCHAR(1024) NOT NULL;
ALTER TABLE appointment_request ADD employee_id BINARY(16) NOT NULL;
ALTER TABLE appointment_request ADD employee_name VARCHAR(1024) NOT NULL;
