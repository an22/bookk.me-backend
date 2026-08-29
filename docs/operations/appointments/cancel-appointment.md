# Cancel appointment

`POST /api/appointments/{id}/cancel` → `CancelAppointment`

Only a scheduled appointment can be cancelled; the status switch and the
write both read the same fetched row within the transaction. The
appointment is fetched before the permission check so a `READ`-level
employee can be let through when it's their own — see [Managing your own
resource on a `READ`
grant](../../object-permissions.md#managing-your-own-resource-on-a-read-grant).

```mermaid
flowchart TD
    Start([POST /api/appointments/id/cancel]) --> PathCheck{path id == body.id?}
    PathCheck -- No --> R400([400 Bad Request])
    PathCheck -- Yes --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Get[AppointmentDataSource.get cancellation.id]
    Get --> Perm{permission >= EDIT, or permission >= READ and appointment.employee.userId == userId?}
    Perm -- No --> R404a([404 Error.OperationNotAllowed])
    Perm -- Yes --> Status{appointment.status}
    Status -- COMPLETED --> R422a([422 ALREADY_COMPLETED 300006])
    Status -- CANCELLED --> R422b([422 ALREADY_CANCELLED 300005])
    Status -- SCHEDULED --> Cancel[AppointmentDataSource.cancel id reason]
    Cancel --> Snapshot[AppointmentSubscriptionDataSource.getBusinessSnapshot businessId]
    Snapshot -- missing --> R404b([404 Error.NotFound - logged as data inconsistency])
    Snapshot -- found --> Event[eventProducer.send AppointmentEvent.Cancelled]
    Event --> R200([200 Cancelled Appointment])
```

**Consumed by:** `AppointmentEvent.Cancelled` → [notifications: notify the
client](../notifications/on-appointment-cancelled.md).
