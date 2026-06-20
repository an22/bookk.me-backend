ALTER TABLE appointment_day_offs ADD start_date DATE NOT NULL;
ALTER TABLE appointment_day_offs ADD end_date DATE NOT NULL;
CREATE INDEX appointment_day_offs_settings_id_start_date ON appointment_day_offs (settings_id, start_date);
ALTER TABLE appointment_day_offs DROP COLUMN `date`;
ALTER TABLE appointment_day_offs DROP INDEX appointment_day_offs_settings_id_date;
