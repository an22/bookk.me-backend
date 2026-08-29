# Decline appointment request

`POST /api/appointments/request/{id}/decline` → `DeclineAppointmentRequest`

Only a pending request can be declined; the current status is checked
inside the same locking read (`getForUpdate`-style) that performs the write,
so a concurrent approval and decline can't both succeed.

```mermaid
flowchart TD
    Start([POST /api/appointments/request/id/decline]) --> PathCheck{path id == body.id?}
    PathCheck -- No --> R400([400 Bad Request])
    PathCheck -- Yes --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{permission >= EDIT for cancellation.businessId?}
    Perm -- No --> R404a([404 Error.OperationNotAllowed])
    Perm -- Yes --> Get[AppointmentRequestDataSource.get cancellation.id]
    Get -- not found --> R404b([404 Error.NotFound])
    Get -- found --> Status{request.status}
    Status -- APPROVED --> R422a([422 REQUEST_ALREADY_APPROVED 300008])
    Status -- DECLINED or CANCELLED --> R422b([422 REQUEST_ALREADY_DECLINED 300007])
    Status -- PENDING --> Decline[AppointmentRequestDataSource.decline id reason]
    Decline --> Snapshot[AppointmentSubscriptionDataSource.getBusinessSnapshot businessId]
    Snapshot -- missing --> R404c([404 Error.NotFound - logged as data inconsistency])
    Snapshot -- found --> Event[eventProducer.send AppointmentEvent.RequestRejected]
    Event --> R204([204 No Content])
```

**Consumed by:** `AppointmentEvent.RequestRejected` → [notifications: notify
the client](../notifications/on-appointment-request-rejected.md).
