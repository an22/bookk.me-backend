[← Database ER diagrams](README.md)

# Appointments service

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
