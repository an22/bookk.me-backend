ALTER TABLE business_has_appointments ADD time_zone VARCHAR(256) NOT NULL;
ALTER TABLE appointment_settings DROP COLUMN time_zone;
