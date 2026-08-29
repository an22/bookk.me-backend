# Edit appointment settings

`PUT /api/appointments/settings/{businessId}` → `EditSettings`

Simple permission-gated update; the working schedule and day-offs live on
the business service and are not touched here (see the `Description` in the
route KDoc).

```mermaid
flowchart TD
    Start([PUT /api/appointments/settings/businessId]) --> PathCheck{path businessId == body.businessId?}
    PathCheck -- No --> R400([400 Bad Request])
    PathCheck -- Yes --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{permission >= EDIT?}
    Perm -- No --> R404([404 Error.OperationNotAllowed])
    Perm -- Yes --> Update[AppointmentSettingsDataSource.update update]
    Update --> R200([200 Updated AppointmentSettings])
```
