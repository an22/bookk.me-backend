# Create appointment from a pending request

`POST /api/appointments` → `CreateAppointment` (via `appointmentRequestId`)

Converts an already-existing `AppointmentRequest` into a confirmed
`Appointment`. Shares its verification logic (workday/worktime/overlap
checks) with [Create instant appointment](create-appointment-instant.md) and
with the auto-approval branch of [Create appointment
request](create-appointment-request.md).

```mermaid
flowchart TD
    Start([POST /api/appointments body AppointmentRequestId]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> GetRequest[AppointmentRequestDataSource.get appointmentRequestId]
    GetRequest -- not found --> R404a([404 Error.NotFound])
    GetRequest -- found --> Settings[AppointmentSettingsDataSource.getForUpdate businessId]
    Settings -- not found --> R404b([404 Error.NotFound])
    Settings -- found --> Perm{permission >= EDIT?}
    Perm -- No --> R404c([404 Error.OperationNotAllowed])
    Perm -- Yes --> PastCheck{request.date < now?}
    PastCheck -- Yes --> R422a([422 DATE_IN_PAST 300012])
    PastCheck -- No --> WorkdayCheck{date within business workday?}
    WorkdayCheck -- No --> R422b([422 DATE_NOT_ALLOWED 300003])
    WorkdayCheck -- Yes --> WorktimeCheck{slot within worktime?}
    WorktimeCheck -- No --> R422c([422 TIME_NOT_ALLOWED 300002])
    WorktimeCheck -- Yes --> Overlap{overlaps another appointment?}
    Overlap -- Yes --> R422d([422 APPOINTMENT_EXISTS 300004])
    Overlap -- No --> Create[AppointmentDataSource.create from request]
    Create --> Approve[AppointmentRequestDataSource.approve request]
    Approve --> Snapshot[AppointmentSubscriptionDataSource.getBusinessSnapshot businessId]
    Snapshot -- missing --> R404d([404 Error.NotFound - logged as data inconsistency])
    Snapshot -- found --> Event[eventProducer.send AppointmentEvent.RequestApproved]
    Event --> R200([200 Created Appointment])
```

**Consumed by:** `AppointmentEvent.RequestApproved` → [notifications: notify
the client](../notifications/on-appointment-request-approved.md).
