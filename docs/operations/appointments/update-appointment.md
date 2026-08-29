# Update appointment (reschedule)

`PUT /api/appointments/{id}` → `UpdateAppointment`

Reschedules an existing appointment. Note the datasource `update` call
happens twice in the current implementation — once before the workday/
worktime/overlap checks and once after — so a caller sees the row written
even if a later check throws; the checks only gate the second write.

```mermaid
flowchart TD
    Start([PUT /api/appointments/id]) --> PathCheck{path id == body.id?}
    PathCheck -- No --> R400([400 Bad Request])
    PathCheck -- Yes --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Settings[AppointmentSettingsDataSource.getForUpdate businessId]
    Settings -- not found --> R404a([404 Error.NotFound])
    Settings -- found --> Perm{permission >= EDIT?}
    Perm -- No --> R404b([404 Error.OperationNotAllowed])
    Perm -- Yes --> PastCheck{appointment.date < now?}
    PastCheck -- Yes --> R422a([422 DATE_IN_PAST 300012])
    PastCheck -- No --> Update1[AppointmentDataSource.update appointment]
    Update1 --> WorkdayCheck{date within business workday?}
    WorkdayCheck -- No --> R422b([422 DATE_NOT_ALLOWED 300003])
    WorkdayCheck -- Yes --> WorktimeCheck{slot within worktime?}
    WorktimeCheck -- No --> R422c([422 TIME_NOT_ALLOWED 300002])
    WorktimeCheck -- Yes --> Overlap{overlaps another appointment?}
    Overlap -- Yes --> R422d([422 APPOINTMENT_EXISTS 300004])
    Overlap -- No --> Update2[AppointmentDataSource.update appointment]
    Update2 --> R200([200 Updated Appointment])
```
