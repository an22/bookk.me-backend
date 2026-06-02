CREATE TABLE IF NOT EXISTS business_has_appointments
(
    id        BINARY(16) PRIMARY KEY,
    createdAt TIMESTAMP(6) NOT NULL,
    updatedAt TIMESTAMP(6) NULL
);
CREATE TABLE IF NOT EXISTS appointment
(
    id               BINARY(16) PRIMARY KEY,
    createdAt        TIMESTAMP(6)  NOT NULL,
    updatedAt        TIMESTAMP(6)  NULL,
    user_id          BINARY(16)    NOT NULL,
    business_id      BINARY(16)    NOT NULL,
    client_id        BINARY(16)    NOT NULL,
    client_name      VARCHAR(1024) NOT NULL,
    client_phone     VARCHAR(24)   NULL,
    client_email     VARCHAR(512)  NULL,
    service_id       BINARY(16)    NOT NULL,
    service_name     VARCHAR(1024) NOT NULL,
    service_group_id BINARY(16)    NOT NULL,
    price_currency   VARCHAR(3)    NOT NULL,
    price_unscaled   BIGINT        NOT NULL,
    price_scale      INT           NOT NULL,
    duration_minutes BIGINT        NOT NULL,
    date_start       TIMESTAMP(6)  NOT NULL,
    date_end         TIMESTAMP(6)  NOT NULL,
    note             VARCHAR(2048) NOT NULL,
    CONSTRAINT fk_appointment_business_id__id FOREIGN KEY (business_id) REFERENCES business_has_appointments (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX appointment_client_id ON appointment (client_id);
CREATE INDEX appointment_service_id ON appointment (service_id);
ALTER TABLE appointment
    ADD CONSTRAINT appointment_user_id_business_id_date_start_unique UNIQUE (user_id, business_id, date_start);
CREATE TABLE IF NOT EXISTS AppointmentSettings
(
    id                          BINARY(16) PRIMARY KEY,
    createdAt                   TIMESTAMP(6)  NOT NULL,
    updatedAt                   TIMESTAMP(6)  NULL,
    business_id                 BINARY(16)    NOT NULL,
    time_zone                   VARCHAR(256)  NOT NULL,
    working_days                TINYINT       NOT NULL,
    in_between_break_in_minutes INT           NOT NULL,
    appointment_note            VARCHAR(2048) NOT NULL,
    CONSTRAINT fk_AppointmentSettings_business_id__id FOREIGN KEY (business_id) REFERENCES business_has_appointments (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
ALTER TABLE AppointmentSettings
    ADD CONSTRAINT AppointmentSettings_business_id_unique UNIQUE (business_id);
CREATE TABLE IF NOT EXISTS appointment_day_offs
(
    id          BINARY(16) PRIMARY KEY,
    createdAt   TIMESTAMP(6) NOT NULL,
    updatedAt   TIMESTAMP(6) NULL,
    settings_id BINARY(16)   NOT NULL,
    `date`      DATE         NOT NULL,
    CONSTRAINT fk_appointment_day_offs_settings_id__id FOREIGN KEY (settings_id) REFERENCES AppointmentSettings (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX appointment_day_offs_settings_id ON appointment_day_offs (settings_id);
CREATE TABLE IF NOT EXISTS appointment_request
(
    id               BINARY(16) PRIMARY KEY,
    createdAt        TIMESTAMP(6)  NOT NULL,
    updatedAt        TIMESTAMP(6)  NULL,
    user_id          BINARY(16)    NOT NULL,
    business_id      BINARY(16)    NOT NULL,
    client_id        BINARY(16)    NOT NULL,
    client_name      VARCHAR(1024) NOT NULL,
    client_phone     VARCHAR(24)   NULL,
    client_email     VARCHAR(512)  NULL,
    service_id       BINARY(16)    NOT NULL,
    service_name     VARCHAR(1024) NOT NULL,
    service_group_id BINARY(16)    NOT NULL,
    price_currency   VARCHAR(3)    NOT NULL,
    price_unscaled   BIGINT        NOT NULL,
    price_scale      INT           NOT NULL,
    duration_minutes BIGINT        NOT NULL,
    date_start       TIMESTAMP(6)  NOT NULL,
    date_end         TIMESTAMP(6)  NOT NULL,
    note             VARCHAR(2048) NOT NULL,
    CONSTRAINT fk_appointment_request_business_id__id FOREIGN KEY (business_id) REFERENCES business_has_appointments (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX appointment_request_client_id ON appointment_request (client_id);
CREATE INDEX appointment_request_service_id ON appointment_request (service_id);
ALTER TABLE appointment_request
    ADD CONSTRAINT appointment_request_user_id_business_id_date_start_unique UNIQUE (user_id, business_id, date_start);
CREATE TABLE IF NOT EXISTS user_has_appointment_permissions
(
    id          BINARY(16) PRIMARY KEY,
    createdAt   TIMESTAMP(6) NOT NULL,
    updatedAt   TIMESTAMP(6) NULL,
    userId      BINARY(16)   NOT NULL,
    business_id BINARY(16)   NOT NULL,
    permission  INT          NOT NULL,
    CONSTRAINT fk_user_has_appointment_permissions_business_id__id FOREIGN KEY (business_id) REFERENCES business_has_appointments (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
ALTER TABLE user_has_appointment_permissions
    ADD CONSTRAINT user_has_appointment_permissions_userId_business_id_unique UNIQUE (userId, business_id);
CREATE TABLE IF NOT EXISTS working_hours
(
    id          BINARY(16) PRIMARY KEY,
    createdAt   TIMESTAMP(6) NOT NULL,
    updatedAt   TIMESTAMP(6) NULL,
    settings_id BINARY(16)   NOT NULL,
    day_of_week TINYINT      NOT NULL,
    start_time  TIME         NOT NULL,
    end_time    TIME         NOT NULL,
    CONSTRAINT fk_working_hours_settings_id__id FOREIGN KEY (settings_id) REFERENCES AppointmentSettings (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX working_hours_settings_id ON working_hours (settings_id);
