[← Database ER diagrams](README.md)

# Authorization service

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
