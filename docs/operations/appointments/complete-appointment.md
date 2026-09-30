# Complete appointment

`POST /api/appointments/{id}/complete` → `CompleteAppointment`

Records that the appointment took place, with `completedBy = USER`. This is
how a business that switched `automaticCompletion` off in its appointment
settings closes its appointments — the `MarkAppointmentsCompleted` job skips
those businesses (see [Scheduled (recurring) jobs](../scheduled-jobs.md)).
It works for businesses with automatic completion on as well, e.g. to close an
appointment before the job reaches it.

Only a `SCHEDULED` appointment that has already started can be completed.
Completing an appointment that is already `COMPLETED` succeeds unchanged —
its existing `completedBy` (possibly `SYSTEM`) is kept. The eligibility check
and the status write share the `SELECT … FOR UPDATE` row lock of
`findByIdAndUpdate`, so a concurrent cancel, no-show or job run cannot slip
in between.

The business is taken from the stored appointment, never from the request.
The appointment is fetched before the permission check so a `view`-only
employee can be let through when it's their own — see [Managing your own
resource on a `view`
grant](../../object-permissions.md#managing-your-own-resource-on-a-view-grant).

```mermaid
flowchart TD
    Start([POST /api/appointments/id/complete]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Get[AppointmentDataSource.get id]
    Get -- missing --> R404a([404 Error.NotFound])
    Get -- found --> Suspended{AppointmentPermissionDataSource.getPermission appointment.businessId - grant row marked suspended?}
    Suspended -- Yes --> R403s([403 BUSINESS_EMPLOYEE_ACCESS_SUSPENDED 200034])
    Suspended -- No --> Perm{caller update permission, or view permission and appointment.employee.userId == userId?}
    Perm -- No --> R404b([404 Error.OperationNotAllowed])
    Perm -- Yes --> Mark[AppointmentDataSource.markCompletedByUser id startedBefore = now - sets COMPLETED and completedBy = USER only when status is SCHEDULED and date_start is not after now]
    Mark --> Status{resulting status}
    Status -- COMPLETED --> R200([200 Completed Appointment])
    Status -- CANCELLED --> R422a([422 ALREADY_CANCELLED 300005])
    Status -- NO_SHOW --> R422c([422 MARKED_NO_SHOW 300019])
    Status -- SCHEDULED --> R422b([422 NOT_STARTED 300018])
```

No event is published.
