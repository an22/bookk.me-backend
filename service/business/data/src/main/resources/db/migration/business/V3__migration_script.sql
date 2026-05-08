ALTER TABLE client ADD email VARCHAR(512) NOT NULL;
CREATE INDEX client_email ON client (email);
ALTER TABLE client ADD CONSTRAINT fk_client_business_id__id FOREIGN KEY (business_id) REFERENCES business(id) ON DELETE CASCADE ON UPDATE RESTRICT;
CREATE INDEX client_phone ON client (phone);
