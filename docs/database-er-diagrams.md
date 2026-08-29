# Database ER Diagrams

bookk-server is a modular monolith split into independently deployable Ktor
microservices. Each microservice owns its own tables (there is no shared
database and no real cross-service foreign key) — cross-service links below
are **logical** references (a plain `uuid` column that happens to hold the id
of a row owned by another service) and are drawn as dashed relationships.

Generated from the Exposed table definitions under
`service/<svc>/data/src/main/kotlin/.../orm/table/`.

- [Authorization service](#authorization-service)
- [User service](#user-service)
- [Business service](#business-service)
- [Appointments service](#appointments-service)
- [Notifications service](#notifications-service)
- [Cross-service overview](#cross-service-overview)

All tables inherit an `id UUID PK`. Tables extending the shared
`BaseUUIDTable` additionally get `createdAt` / `updatedAt` (nullable); tables
built directly on Exposed's `UuidTable` declare their own timestamp columns
where present — both are shown per table below.

---

## Authorization service

Owns login/session/device and WebAuthn passkey state, keyed off `authentication.user_id`
(a logical reference to `user.profile.id`).

```mermaid
erDiagram
    AUTHENTICATION ||--o{ AUTH_DEVICE : "has"
    AUTHENTICATION ||--o{ PASSKEY_CREDENTIALS : "has"

    AUTHENTICATION {
        uuid id PK
        uuid user_id UK "logical FK -> user.profile.id"
        uuid uuid UK
        timestamp created_at
        timestamp updated_at
    }

    AUTH_DEVICE {
        uuid id PK
        uuid user_auth_id FK
        uuid device_uuid UK "composite with user_auth_id"
        string device_name
        enum language
        uuid refresh_token_id
        string refresh_token_hash
        timestamp refresh_token_expires_at
        uuid previous_refresh_token_id
        string previous_refresh_token_hash
        bool is_signed_in
        timestamp last_log_in_at
        timestamp created_at
        timestamp updated_at
    }

    PASSKEY_CREDENTIALS {
        uuid id PK
        uuid auth_id FK
        string name
        binary cred_descriptor_id UK "composite with auth_id"
        string cred_descriptor_type
        string cred_descriptor_transports
        text public_key
        long signature_count
        bool discoverable
        bool backup_eligible
        bool backed_up
        binary attestation_object
        string client_data
        timestamp created_at
        timestamp updated_at
        timestamp last_used_at
    }
```

---

## User service

Smallest schema — the canonical user profile and inbound contact-form submissions.

```mermaid
erDiagram
    PROFILE ||--o{ CONTACT_FORM : "submits"

    PROFILE {
        uuid id PK
        string name
        string last_name
        string email UK
        string phone
        timestamp created_at
        timestamp updated_at
    }

    CONTACT_FORM {
        uuid id PK
        uuid user_id FK
        text text
        text usage_logs
        byte status
        timestamp created_at
        timestamp updated_at
    }
```

---

## Business service

Largest schema — a business, its staff, services and clients.

```mermaid
erDiagram
    BUSINESS ||--o{ BUSINESS_DASHBOARD : "has"
    BUSINESS ||--o{ BUSINESS_DAY_OFFS : "has"
    BUSINESS ||--o{ BUSINESS_PERMISSIONS : "grants"
    BUSINESS ||--o{ BUSINESS_WORKING_HOURS : "has"
    BUSINESS ||--o{ CLIENT : "has"
    BUSINESS ||--o{ EMPLOYEE : "employs"
    BUSINESS ||--o{ EMPLOYEE_INVITATION : "sends"
    BUSINESS ||--o{ SERVICE_GROUP : "defines"
    BUSINESS ||--o{ SERVICE : "offers"
    BUSINESS ||--o{ EMPLOYEE_CAN_PROVIDE_SERVICE : "scopes"
    EMPLOYEE ||--o{ EMPLOYEE_CAN_PROVIDE_SERVICE : "can provide"
    SERVICE ||--o{ EMPLOYEE_CAN_PROVIDE_SERVICE : "provided via"
    EMPLOYEE ||--o{ EMPLOYEE_DAY_OFFS : "has"
    EMPLOYEE ||--o{ EMPLOYEE_WORKING_HOURS : "has"
    SERVICE_GROUP ||--o{ SERVICE : "groups"

    BUSINESS {
        uuid id PK
        uuid user_id UK "logical FK -> user.profile.id (owner)"
        string name
        string description
        string address
        double latitude
        double longitude
        string currency
        string timezone
        byte working_days
        string instagram
        string telegram
        string viber
        string whatsapp
        string phone
        timestamp created_at
        timestamp updated_at
    }

    BUSINESS_DASHBOARD {
        uuid id PK
        uuid user_id UK "logical FK -> user.profile.id"
        uuid business_id FK
        timestamp created_at
        timestamp updated_at
    }

    BUSINESS_DAY_OFFS {
        uuid id PK
        uuid business_id FK
        date start_date
        date end_date
        timestamp created_at
        timestamp updated_at
    }

    BUSINESS_PERMISSIONS {
        uuid id PK
        uuid user_id "logical FK -> user.profile.id; UK with business_id"
        uuid business_id FK
        int permission
    }

    BUSINESS_WORKING_HOURS {
        uuid id PK
        uuid business_id FK
        byte day_of_week
        time start_time
        time end_time
        timestamp created_at
        timestamp updated_at
    }

    CLIENT {
        uuid id PK
        uuid business_id FK
        string name
        string lastname
        string phone
        string email
        uuid user_id "logical FK -> user.profile.id, nullable"
        timestamp source_updated_at
        timestamp created_at
        timestamp updated_at
    }

    EMPLOYEE {
        uuid id PK
        uuid business_id FK
        string name
        string lastname
        string phone
        string email
        uuid user_id "logical FK -> user.profile.id"
        timestamp source_updated_at
        byte working_days
        timestamp created_at
        timestamp updated_at
    }

    EMPLOYEE_CAN_PROVIDE_SERVICE {
        uuid id PK
        uuid employee_id FK "UK with service_id"
        uuid service_id FK
        uuid business_id FK
    }

    EMPLOYEE_DAY_OFFS {
        uuid id PK
        uuid employee_id FK
        date start_date
        date end_date
        timestamp created_at
        timestamp updated_at
    }

    EMPLOYEE_INVITATION {
        uuid id PK
        uuid business_id FK "UK with email"
        uuid invited_by "logical FK -> user.profile.id"
        string email
        enum status
        timestamp created_at
        timestamp updated_at
    }

    EMPLOYEE_WORKING_HOURS {
        uuid id PK
        uuid employee_id FK
        byte day_of_week
        time start_time
        time end_time
        timestamp created_at
        timestamp updated_at
    }

    SERVICE_GROUP {
        uuid id PK
        uuid business_id FK "UK with name"
        string name
        timestamp created_at
        timestamp updated_at
    }

    SERVICE {
        uuid id PK
        uuid business_id FK "UK with group_id, name"
        uuid group_id FK
        string name
        int duration
        string price_currency
        long price_unscaled
        int price_scale
        bool available
        timestamp created_at
        timestamp updated_at
    }
```

---

## Appointments service

Keeps its own denormalized copy of business identity (`business_has_appointments`,
populated from the business service via events) so booking logic never has to
cross a service boundary at read time.

```mermaid
erDiagram
    BUSINESS_HAS_APPOINTMENTS ||--o{ APPOINTMENT : "hosts"
    BUSINESS_HAS_APPOINTMENTS ||--o{ APPOINTMENT_REQUEST : "hosts"
    BUSINESS_HAS_APPOINTMENTS ||--o{ APPOINTMENT_DAY_OFFS : "has"
    BUSINESS_HAS_APPOINTMENTS ||--|| APPOINTMENT_SETTINGS : "configures"
    BUSINESS_HAS_APPOINTMENTS ||--o{ WORKING_HOURS : "has"
    BUSINESS_HAS_APPOINTMENTS ||--o{ USER_HAS_APPOINTMENT_PERMISSIONS : "grants"
    APPOINTMENT ||--o{ APPOINTMENT_SERVICES : "line items"
    APPOINTMENT_REQUEST ||--o{ APPOINTMENT_REQUEST_SERVICES : "line items"

    BUSINESS_HAS_APPOINTMENTS {
        uuid id PK "= business.id (replicated)"
        bool enabled
        string name
        string address
        string time_zone
        byte working_days
        timestamp source_updated_at
        timestamp createdAt
        timestamp updatedAt
    }

    APPOINTMENT {
        uuid id PK
        uuid user_id "logical FK -> user.profile.id; UK with business_id, date_start"
        uuid business_id FK
        uuid employee_id "logical FK -> business.employee.id"
        string employee_name
        uuid client_id "logical FK -> business.client.id"
        string client_name
        string client_phone
        string client_email
        timestamp date_start
        timestamp date_end
        string note
        enum status
        string cancellation_reason
        timestamp createdAt
        timestamp updatedAt
    }

    APPOINTMENT_SERVICES {
        uuid id PK
        uuid appointment_id FK
        uuid service_id "logical FK -> business.service.id"
        string service_name
        uuid service_group_id "logical FK -> business.service_group.id"
        string price_currency
        long price_unscaled
        int price_scale
        long duration_minutes
        timestamp createdAt
        timestamp updatedAt
    }

    APPOINTMENT_REQUEST {
        uuid id PK
        uuid user_id "logical FK -> user.profile.id; UK with business_id, date_start"
        uuid business_id FK
        uuid employee_id "logical FK -> business.employee.id"
        string employee_name
        uuid client_id "logical FK -> business.client.id"
        string client_name
        string client_phone
        string client_email
        timestamp date_start
        timestamp date_end
        string note
        enum status
        string decline_reason
        timestamp createdAt
        timestamp updatedAt
    }

    APPOINTMENT_REQUEST_SERVICES {
        uuid id PK
        uuid appointment_request_id FK
        uuid service_id "logical FK -> business.service.id"
        string service_name
        uuid service_group_id "logical FK -> business.service_group.id"
        string price_currency
        long price_unscaled
        int price_scale
        long duration_minutes
        timestamp createdAt
        timestamp updatedAt
    }

    APPOINTMENT_DAY_OFFS {
        uuid id PK
        uuid business_id FK
        date start_date
        date end_date
        timestamp createdAt
        timestamp updatedAt
    }

    APPOINTMENT_SETTINGS {
        uuid id PK
        uuid business_id FK,UK
        int in_between_break_in_minutes
        string appointment_note
        bool automatic_approval
        timestamp createdAt
        timestamp updatedAt
    }

    WORKING_HOURS {
        uuid id PK
        uuid business_id FK
        byte day_of_week
        time start_time
        time end_time
        timestamp createdAt
        timestamp updatedAt
    }

    USER_HAS_APPOINTMENT_PERMISSIONS {
        uuid id PK
        uuid userId "logical FK -> user.profile.id; UK with business_id"
        uuid business_id FK
        int permission
        timestamp createdAt
        timestamp updatedAt
    }
```

---

## Notifications service

Fan-in tables keyed by `user_id`, feeding channel dispatch for a given user across devices/targets.

```mermaid
erDiagram
    NOTIFICATION_SETTINGS ||--o{ NOTIFICATION_CHANNELS : "has"

    DEVICE {
        uuid id PK
        uuid auth_id "logical FK -> authentication.id; UK with device_uuid, user_id"
        uuid device_uuid UK
        uuid user_id "logical FK -> user.profile.id"
        text notification_token
        enum language
        timestamp createdAt
        timestamp updatedAt
    }

    NOTIFICATION_SETTINGS {
        uuid id PK
        uuid user_id UK "logical FK -> user.profile.id"
        bool appointment_enabled
        timestamp createdAt
        timestamp updatedAt
    }

    NOTIFICATION_CHANNELS {
        uuid id PK
        uuid settings_id FK "UK with channel"
        enum channel
        bool enabled
        bool available_to_clients
        timestamp createdAt
        timestamp updatedAt
    }

    NOTIFICATION_EMAIL_TARGETS {
        uuid id PK
        uuid user_id UK "logical FK -> user.profile.id"
        text email
        timestamp source_updated_at
        timestamp createdAt
        timestamp updatedAt
    }

    NOTIFICATION_TELEGRAM_TARGETS {
        uuid id PK
        uuid user_id UK "logical FK -> user.profile.id"
        text telegram_tag
        timestamp createdAt
        timestamp updatedAt
    }
```

`notification_email_targets` and `notification_telegram_targets` are not
foreign-keyed to `notification_settings` in the schema — they are joined to a
user's settings at query time via the shared `user_id`.

---

## Cross-service overview

No database enforces these edges — each service has its own schema/connection
pool, and the referencing column is a plain `uuid`. Consistency is maintained
via domain events (see `service/<svc>/client`) rather than DB constraints.

```mermaid
erDiagram
    PROFILE ||--o| AUTHENTICATION : "user_id"
    PROFILE ||--o| BUSINESS : "user_id (owner)"
    PROFILE ||--o{ EMPLOYEE : "user_id"
    PROFILE ||--o{ CLIENT : "user_id (nullable)"
    PROFILE ||--o{ BUSINESS_PERMISSIONS : "user_id"
    PROFILE ||--o{ USER_HAS_APPOINTMENT_PERMISSIONS : "userId"
    PROFILE ||--o{ NOTIFICATION_SETTINGS : "user_id"
    PROFILE ||--o{ DEVICE : "user_id"
    AUTHENTICATION ||--o{ DEVICE : "auth_id"
    BUSINESS ||--|| BUSINESS_HAS_APPOINTMENTS : "id (replicated via events)"
    EMPLOYEE ||--o{ APPOINTMENT : "employee_id"
    CLIENT ||--o{ APPOINTMENT : "client_id"
    SERVICE ||--o{ APPOINTMENT_SERVICES : "service_id"
    SERVICE_GROUP ||--o{ APPOINTMENT_SERVICES : "service_group_id"

    PROFILE {
        uuid id PK
    }
    AUTHENTICATION {
        uuid id PK
    }
    DEVICE {
        uuid id PK
    }
    BUSINESS {
        uuid id PK
    }
    BUSINESS_HAS_APPOINTMENTS {
        uuid id PK
    }
    EMPLOYEE {
        uuid id PK
    }
    CLIENT {
        uuid id PK
    }
    SERVICE {
        uuid id PK
    }
    SERVICE_GROUP {
        uuid id PK
    }
    BUSINESS_PERMISSIONS {
        uuid id PK
    }
    USER_HAS_APPOINTMENT_PERMISSIONS {
        uuid id PK
    }
    NOTIFICATION_SETTINGS {
        uuid id PK
    }
    APPOINTMENT {
        uuid id PK
    }
    APPOINTMENT_SERVICES {
        uuid id PK
    }
```
