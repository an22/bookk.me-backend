# React to an employee permissions change

Kafka topic `BusinessEvent.EmployeePermissionsChanged` → `AppointmentEventHandler` → `SyncEmployeePermission`

Produced by [Join business](../business/join-business.md) (grants
view-only on every resource) and [Set employee
permission](../business/set-employee-permission.md) (grants whatever
`ResourcePermission` was requested, for one resource). Keeps the
appointments service's own copy of the grant
(`appointment_permission_grants`) in sync with the business service's
source of truth, reading just the `appointments` field off the published
`BusinessPermissions` — every other resource in the payload is ignored, so
an employee's appointments access matches their business-service grant
without an inline cross-service call on every appointments request. A
`view`-only grant lets the employee manage only their own
appointments/requests; see [Managing your own resource on a `view`
grant](../../object-permissions.md#managing-your-own-resource-on-a-view-grant).

```mermaid
flowchart TD
    Start([Consume BusinessEvent.EmployeePermissionsChanged]) --> Dedup{idempotencyKey already processed?}
    Dedup -- Yes --> Skip([Skip - already handled])
    Dedup -- No --> Enabled{AppointmentSubscriptionDataSource.isBusinessEnabled businessId?}
    Enabled -- No --> NoOp([No-op - nothing to sync yet, acked as processed])
    Enabled -- Yes --> Set[AppointmentPermissionDataSource.setPermission employeeUserId businessId event.permissions.appointments - upsert]
    Set -- throws --> Dlt([Sent to BusinessEvent.EmployeePermissionsChanged_dlt topic, logged - no auto-retry])
    Set -- ok --> Ack([Marked processed])
```

An employee granted or promoted before their business enables appointments
gets no row here until their permissions change again after that point —
there is no backfill on enable.
