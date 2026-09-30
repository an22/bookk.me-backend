# Mark appointment as no-show

`POST /api/appointments/{id}/no-show` → `MarkAppointmentNoShow`

Records that the client missed the appointment. Only an appointment that has
already started can be marked, and it may be either still `SCHEDULED` or
already `COMPLETED` — the `MarkAppointmentsCompleted` job flips every past
appointment of a business with `automaticCompletion` on to `COMPLETED`, and
staff can [complete](complete-appointment.md) one by hand, so a no-show is
often recorded after that. Marking a `COMPLETED` appointment as no-show clears
its `completedBy`.
Marking an appointment that is already `NO_SHOW` succeeds unchanged. The
eligibility check and the status write share the `SELECT … FOR UPDATE` row
lock of `findByIdAndUpdate`, so a concurrent cancel cannot slip in between.

The business is taken from the stored appointment, never from the request.
The appointment is fetched before the permission check so a `view`-only
employee can be let through when it's their own — see [Managing your own
resource on a `view`
grant](../../object-permissions.md#managing-your-own-resource-on-a-view-grant).

```mermaid
flowchart TD
    Start([POST /api/appointments/id/no-show]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Get[AppointmentDataSource.get id]
    Get -- missing --> R404a([404 Error.NotFound])
    Get -- found --> Suspended{AppointmentPermissionDataSource.getPermission appointment.businessId - grant row marked suspended?}
    Suspended -- Yes --> R403s([403 BUSINESS_EMPLOYEE_ACCESS_SUSPENDED 200034])
    Suspended -- No --> Perm{caller update permission, or view permission and appointment.employee.userId == userId?}
    Perm -- No --> R404b([404 Error.OperationNotAllowed])
    Perm -- Yes --> Mark[AppointmentDataSource.markNoShow id eligibleStatuses = SCHEDULED, COMPLETED startedBefore = now - sets NO_SHOW and clears completedBy only when status is in eligibleStatuses and date_start is not after now]
    Mark --> Status{resulting status}
    Status -- NO_SHOW --> R200([200 No-show Appointment])
    Status -- CANCELLED --> R422a([422 ALREADY_CANCELLED 300005])
    Status -- SCHEDULED or COMPLETED --> R422b([422 NOT_STARTED 300018])
```

No event is published.
