# Resource permissions

Every business-scoped operation checks a caller's grant for a specific
**resource** before doing anything else. The grant is a set of three
independent booleans — `view` / `update` / `delete` — defined once in
`library/permissions`
(`library/permissions/src/main/kotlin/library/permissions/ResourcePermission.kt`)
and shared by every microservice. Unlike the old single ranked level this
replaces, each action is granted independently: an employee can hold
`update` on `CLIENTS` without holding `view` on `EMPLOYEES`, or `view` +
`update` without `delete` on the same resource.

```kotlin
enum class PermissionAction { VIEW, UPDATE, DELETE }

data class ResourcePermission(
    val view: Boolean = false,
    val update: Boolean = false,
    val delete: Boolean = false
) {
    fun grants(action: PermissionAction): Boolean
    fun covers(other: ResourcePermission): Boolean   // true if every bit `other` has, this also has
    companion object { val NONE; val FULL }
}
```

## How a check works

An operation calls `.assert(PermissionAction.X)` on the caller's stored
grant for the resource in question:

```kotlin
businessPermissionDataSource.getPermission(userId, businessId, BusinessResource.CLIENTS)
    .assert(PermissionAction.UPDATE)
```

`assert` throws `com.bookk.core.domain.entity.Error.OperationNotAllowed`
unless the stored grant has that specific bit set — a missing grant row
resolves to `ResourcePermission.NONE` (every bit `false`), so it always
fails. There is no ranking between actions: holding `delete` does not imply
`update` or `view` — each is granted explicitly, on purpose, per resource.

### Managing your own resource on a `view` grant

A second extension, `ResourcePermission?.assertOrSelf(action, actorId,
assigneeId)`, lets a caller who only holds `view` pass an `update` check
when the resource being mutated is assigned to them: it passes when the
grant already grants `action`, **or** when the grant has `view = true` and
`actorId == assigneeId`. Appointments uses this for an employee acting on
their own appointment/request — `CreateAppointmentImpl`,
`UpdateAppointmentImpl`, `CancelAppointmentImpl`, and
`DeclineAppointmentRequestImpl` all pass the caller's `userId` as `actorId`
and the appointment/request's `employee.userId` as `assigneeId`
(`CreateAppointmentRequestImpl`, the client-facing booking entry point, has
no permission gate at all — see its KDoc). A caller who holds `update`
directly is unaffected — they can still act on anyone's appointment. A
`view`-only holder acting on an appointment assigned to a different
employee still gets `OperationNotAllowed`.

`OperationNotAllowed` is a generic infrastructure error, not a
`BusinessError`, so `call.respondWith(result)` maps it to **HTTP 404**, not
403 — a permission failure looks identical to the object simply not
existing (see `core/service`'s `respondWith`).

## Resources

The business service defines five resources in
`com.bookk.business.domain.api.business.entity.BusinessResource`:
`BUSINESS`, `EMPLOYEES`, `CLIENTS`, `SERVICES`, `APPOINTMENTS`. A grant is
always scoped to exactly one `(userId, businessId, resource)` triple.
`APPOINTMENTS` is owned and assigned here too (an employee's appointment
access is managed from the business service's employee screen), but it is
enforced by the appointments service against its own local copy — see
[Storage](#storage).

The appointments service has a single implicit resource (its own
"appointments" grant, covering both settings and appointment records) and
so does not need its own `Resource` enum — `AppointmentPermissionDataSource`
is keyed by `(userId, businessId)` alone.

| Resource | Action | Required by |
|---|---|---|
| `BUSINESS` | `update` | Update the business profile (`UpdateBusinessImpl`) |
| `BUSINESS` | full control (`covers(FULL)`) | Enable the appointments module for a business, checked cross-service (`EnableAppointmentsForBusinessImpl`) |
| `EMPLOYEES` | `view` | List employees (`GetEmployeesImpl`), read an employee's permissions (`GetEmployeePermissionsImpl`) |
| `EMPLOYEES` | `update` | Update an employee's own record (`UpdateEmployeeImpl`), create/revoke an employee invitation (`CreateEmployeeInvitationImpl`, `RevokeEmployeeInvitationImpl`), grant or revoke another resource's permission for an employee (`SetEmployeePermissionImpl`) |
| `CLIENTS` | `view` | List clients (`GetClientsImpl`) |
| `CLIENTS` | `update` | Create/update a client (`CreateClientImpl`, `UpdateClientImpl`) |
| `CLIENTS` | `delete` | Delete a client (`DeleteClientImpl`) |
| `SERVICES` | `update` | Create/update a service or service group (`CreateServiceImpl`, `UpdateServiceImpl`, `CreateServiceGroupImpl`) |
| `SERVICES` | `delete` | Delete a service or service group (`DeleteServiceImpl`, `DeleteServiceGroupImpl`) |
| appointments (local) | `view` | Get appointment settings/history/requests, check whether appointments are enabled (`GetSettingsImpl`, `GetAppointmentHistoryImpl`, `GetAppointmentsForDataImpl`, `GetAppointmentRequestsImpl`, `GetPendingAppointmentRequestsImpl`, `IsAppointmentsEnabledImpl`) |
| appointments (local) | `update` | Edit appointment settings, create/update/cancel/decline **any** employee's appointment (`EditSettingsImpl`, `CreateAppointmentImpl`, `UpdateAppointmentImpl`, `CancelAppointmentImpl`, `DeclineAppointmentRequestImpl`) — an employee with only `view` can still do this for their own, see [above](#managing-your-own-resource-on-a-view-grant) |

## Granting and revoking permissions

There is no fixed role (the old `EMPLOYEE`/`MANAGER` split is gone).
Instead, an owner (or anyone holding `EMPLOYEES.update`) grants or revokes
one resource's `view`/`update`/`delete` bits for one employee at a time:

```
PUT /api/business/{businessId}/employee/{id}/permissions/{resource}
Body: library.permissions.ResourcePermission
```

`SetEmployeePermissionImpl` requires the caller to hold `EMPLOYEES.update`,
looks up the target employee, and additionally requires the caller's own
grant on the **target resource** to `.covers()` the permission being handed
out — you cannot grant delegate access you don't hold yourself
(`SetEmployeePermission.Error.InsufficientGrant`, 422). A companion read
endpoint, `GET /api/business/{businessId}/employee/{id}/permissions`
(`GetEmployeePermissionsImpl`, requires `EMPLOYEES.view`), returns the
employee's current grants across all five resources as
`BusinessPermissions`, letting a management UI pre-fill toggles.

| Grant | Where | Result |
|---|---|---|
| Business creator | `CreateBusinessImpl` | `ResourcePermission.FULL` on all five resources |
| Employee who joined | `JoinBusinessImpl` | `view = true` (nothing else) on all five resources — customizable afterward via `SetEmployeePermission` |
| Employee's permission changed by an authorized caller | `SetEmployeePermissionImpl` | Whatever `ResourcePermission` was requested, for the one resource specified |

Every row above that changes an **existing** employee's grants (join, or an
explicit set) publishes `BusinessEvent.EmployeePermissionsChanged`
(`employeeUserId`, `businessId`, `permissions: BusinessPermissions` — the
employee's full, current grant set across all five resources, not just the
one that changed), which the appointments service consumes to keep its own
local copy of the `APPOINTMENTS` grant in sync — see
[Storage](#storage).

## Cross-service checks

A service that needs to check another business's grant without fetching
the whole `Business` entity calls the internal, lean endpoint:

```
GET /api/internal/business/{id}/permissions/{userId}/{resource}
Response: library.permissions.ResourcePermission
```

backed by `GetBusinessPermissionImpl` and exposed to other services through
`BusinessClient.getPermission(userId, businessId, resource)`. This is how
`EnableAppointmentsForBusinessImpl` checks whether the caller has full
control (`covers(ResourcePermission.FULL)`) of `BusinessResource.BUSINESS`
before turning the appointments module on for a business — the
fine-grained equivalent of the old "must be `OWNER`" gate.

## Storage

This is a modular monolith: each microservice persists its own copy of the
grants it needs rather than sharing one table across services.

- **Business service** owns the source-of-truth grants, in
  `business_permission_grants` (`BusinessPermissionDataSourceImpl`), one row
  per `(userId, businessId, resource)`, with `can_view`/`can_update`/
  `can_delete` boolean columns. `getPermissions(userId, businessId)`
  aggregates all five rows into one `BusinessPermissions`; missing rows
  default to `ResourcePermission.NONE`. This is also what gets embedded in
  the `Business` entity as `Business.permissions` — `GetBusinessById`,
  `GetDashboardBusiness`, and `GetUserBusinesses` all attach the requesting
  user's grants onto the business (or businesses) they return, computed
  per request rather than stored on the business row itself.
- **Appointments service** keeps its own local copy in
  `appointment_permission_grants` (`AppointmentPermissionDataSourceImpl`),
  one row per `(userId, businessId)` with the same three boolean columns —
  a single implicit resource, no `resource` column needed. This is embedded
  in `AppointmentSettings.permissions`, attached by `GetSettingsImpl` and
  `EditSettingsImpl` the same way `Business.permissions` is. The business
  owner's row is written once, when the business enables appointments
  (`EnableAppointmentsForBusinessImpl`, seeded with `ResourcePermission.FULL`
  after the cross-service check above succeeds). An employee's row is
  written/overwritten by `SyncEmployeePermission`, called from
  `AppointmentEventHandler` in reaction to
  `BusinessEvent.EmployeePermissionsChanged` (reading just the `appointments`
  field off the published `BusinessPermissions`), and is a no-op if the
  business hasn't enabled appointments yet
  (`AppointmentSubscriptionDataSource.isBusinessEnabled`) — an employee
  granted or promoted before their business turns appointments on gets no
  row until their permissions change again after that point. See
  [React to an employee permissions
  change](operations/appointments/on-employee-permissions-changed.md) for
  the full flow.

Both stores use the same three-boolean shape, so `ResourcePermission`,
`.assert(...)`, `.assertOrSelf(...)`, and `.covers(...)` behave identically
regardless of which service's table backs the check.
