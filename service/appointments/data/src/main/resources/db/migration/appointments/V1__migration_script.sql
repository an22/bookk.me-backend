CREATE TABLE IF NOT EXISTS business_has_appointments (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL);
CREATE TABLE IF NOT EXISTS user_has_appointment_permissions (id BINARY(16) PRIMARY KEY, createdAt TIMESTAMP(6) NOT NULL, updatedAt TIMESTAMP(6) NULL, userId BINARY(16) NOT NULL, businessId BINARY(16) NOT NULL, permission INT NOT NULL);
ALTER TABLE user_has_appointment_permissions ADD CONSTRAINT user_has_appointment_permissions_userId_businessId_unique UNIQUE (userId, businessId);
