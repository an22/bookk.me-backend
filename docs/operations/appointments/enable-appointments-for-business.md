# Enable appointments for business

`POST /api/appointments/enabled/{businessId}` → `EnableAppointmentsForBusiness`

Turns the appointments plugin on for a business: pulls a snapshot of the
business (name, address, time zone, schedule) from the business service,
subscribes it locally, seeds the caller as `OWNER` in the appointments
service's own permission table, and creates default settings. All three
writes share one transaction; a repeat call collides on the subscription's
unique constraint and surfaces as `PLUGIN_ALREADY_ENABLED`.

```mermaid
flowchart TD
    Start([POST /api/appointments/enabled/businessId]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> GetPerm[BusinessClient.getPermission userId businessId]
    GetPerm -- request failed --> RErr([Propagate business-service error])
    GetPerm -- ok --> GetBiz[BusinessClient.getBusinessById businessId]
    GetBiz -- request failed --> RErr
    GetBiz -- ok --> Tx[[Begin transaction]]
    Tx --> AssertOwner{permission == OWNER?}
    AssertOwner -- No --> R404([404 Error.OperationNotAllowed])
    AssertOwner -- Yes --> Attach[AppointmentSubscriptionDataSource.attachBusiness BusinessSnapshot]
    Attach --> InitPerm[PermissionsDataSource.initPermissions userId businessId OWNER]
    InitPerm --> CreateSettings[AppointmentSettingsDataSource.create default AppointmentSettings]
    CreateSettings --> Constraint{Unique constraint violated?}
    Constraint -- Yes --> R422([422 PLUGIN_ALREADY_ENABLED 300009])
    Constraint -- No --> R204([204 No Content])
```
