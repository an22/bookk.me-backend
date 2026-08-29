# Object permissions

Every business-scoped operation checks a caller's permission level for a
given `businessId` before doing anything else. The level lives in
`library/permissions`
(`library/permissions/src/main/kotlin/library/permissions/ObjectPermission.kt`)
and is a single ranked enum shared by every microservice — each service
keeps its own copy of the grant (see [Storage](#storage) below), but the
levels and their meaning are the same everywhere.

```kotlin
enum class ObjectPermission(val int: Int) {
    NONE(0),
    READ(1),
    EDIT(2),
    OWNER(100);
}
```

## How a check works

An operation calls `.assert(ObjectPermission.X)` on the caller's stored
grant (an `Int?` or `ObjectPermission?`, both extension functions in the
same file):

```kotlin
permissionsDataSource.getPermissions(userId, businessId).assert(ObjectPermission.EDIT)
```

`assert` is a **minimum-level** check: it throws
`com.bookk.core.domain.entity.Error.OperationNotAllowed` unless the stored
grant's `int` is `>=` the requested level's `int` — a missing grant (`null`)
always fails. Because levels are ranked by `int` rather than compared by
equality, a higher level automatically satisfies any check for a lower one:
`OWNER` (100) passes an `EDIT` (2) or `READ` (1) check, `EDIT` passes a
`READ` check, and so on. There is no independent "read-only" vs
"write-only" axis — permissions form a single ladder.

`OperationNotAllowed` is a generic infrastructure error, not a
`BusinessError`, so `call.respondWith(result)` maps it to **HTTP 404**, not
403 — a permission failure looks identical to the object simply not
existing (see `core/service`'s `respondWith`).

## The levels

Each level below lists **only** the operations that require exactly that
minimum — because `.assert()` is a minimum-level check, every level also
grants everything listed under the levels above it (`EDIT` can do
everything `READ` can; `OWNER` can do everything `EDIT` and `READ` can).

### `NONE` (0)

No grant at all. This is what `ObjectPermission.of(null)` resolves to and
what an `.assert()` check fails against — it is never assigned to a user on
purpose, it is the absence of a row. No operation is allowed.

### `READ` (1)

Can view the business's data but not change it.

- **Appointments**
  - Get appointment settings (`GetSettingsImpl`)
  - Get appointment history (`GetAppointmentHistoryImpl`)
  - Get appointments for a date (`GetAppointmentsForDataImpl`)
  - Get appointment requests (`GetAppointmentRequestsImpl`)
  - List pending appointment requests (`GetPendingAppointmentRequestsImpl`)
  - Check whether appointments are enabled for the business
    (`IsAppointmentsEnabledImpl`)
- **Business**
  - List clients (`GetClientsImpl`)

This is also the level granted automatically to an **employee** once their
invitation is approved (`ApproveEmployeeInvitationImpl`) or when an owner
demotes/sets them to the `Employee` role via the promote-employee route
(see [Roles](#roles-employee--manager) below).

### `EDIT` (2)

Can create, update, or cancel/delete the business's data, in addition to
everything `READ` allows.

- **Appointments**
  - Create an appointment (`CreateAppointmentImpl`)
  - Update/reschedule an appointment (`UpdateAppointmentImpl`)
  - Cancel an appointment (`CancelAppointmentImpl`)
  - Create an appointment request (`CreateAppointmentRequestImpl`)
  - Decline an appointment request (`DeclineAppointmentRequestImpl`)
  - Edit appointment settings (`EditSettingsImpl`)
- **Business**
  - Update the business profile (`UpdateBusinessImpl`)
  - Create/update/delete a service (`CreateServiceImpl`,
    `UpdateServiceImpl`, `DeleteServiceImpl`)
  - Create/delete a service group (`CreateServiceGroupImpl`,
    `DeleteServiceGroupImpl`)
  - Create/delete a client (`CreateClientImpl`, `DeleteClientImpl`)
  - Update an employee's own record — profile, schedule, provided services
    (`UpdateEmployeeImpl`)

This is the level granted when an owner sets an employee to the `Manager`
role via the promote-employee route (see
[Roles](#roles-employee--manager)).

### `OWNER` (100)

Full control, including operations that affect who else has access or that
initialize a business's presence in a service.

- **Business**
  - Create an employee invitation (`CreateEmployeeInvitationImpl`) — only
    the owner can invite new employees
  - Promote an employee to `Employee` or `Manager` (`PromoteEmployeeImpl`,
    see [Roles](#roles-employee--manager)) — only the owner can change
    another user's permission level
- **Appointments**
  - Enable the appointments module for a business
    (`EnableAppointmentsForBusinessImpl`)

`OWNER`'s value (100) is set far above `EDIT`/`READ` deliberately, leaving
room to insert intermediate levels (e.g. a future `MANAGE`) between `EDIT`
and `OWNER` later without renumbering the existing ones.

## Roles: Employee / Manager

The permission levels above are the raw `ObjectPermission` ladder used
internally by every service. The business service's HTTP API does not
expose `READ`/`EDIT`/`OWNER` directly to callers changing another user's
access — instead `POST /api/business/{businessId}/employee/{id}/promote`
takes a named `role`, defined in
`com.bookk.business.domain.api.employee.entity.EmployeeRole`:

| Role | Maps to | Can do |
|---|---|---|
| `EMPLOYEE` | `ObjectPermission.READ` | Everything under [`READ`](#read-1) |
| `MANAGER` | `ObjectPermission.EDIT` | Everything under [`READ`](#read-1) and [`EDIT`](#edit-2) |

`EmployeeRole.toPermission()` performs the mapping; the route (`Employee.kt`
→ `employeeCrud()`) requires the caller to hold `OWNER` on the business,
looks up the target `Employee` by `id` to resolve their `userId`, then
calls `businessDataSource.setUserPermissions(employee.userId, businessId,
role.toPermission().int)`. There is no `OWNER`-level role exposed through
this route — ownership is only ever granted at business creation (see
below) and cannot be promoted to.

## How grants are created

| Grant | Where | Level |
|---|---|---|
| Business creator | `CreateBusinessImpl` → `businessDataSource.setUserPermissions(userId, businessId, ObjectPermission.OWNER.int)` | `OWNER` |
| Approved employee | `ApproveEmployeeInvitationImpl` → `businessDataSource.setUserPermissions(requestUserId, businessId, ObjectPermission.READ.int)` | `READ` |
| Employee promoted/demoted by the owner | `PromoteEmployeeImpl` → `businessDataSource.setUserPermissions(employee.userId, businessId, role.toPermission().int)` | `READ` or `EDIT`, per the request's `role` |

The first two rows only ever create a grant once, for a specific user; the
promote route is the only operation that changes an **existing** grant, and
only ever between `READ` and `EDIT` — it cannot grant or revoke `OWNER`.

## Storage

This is a modular monolith: each microservice persists its own copy of the
grant rather than sharing one table across services.

- **Business service** owns the source-of-truth grant, in
  `BusinessPermissionsTable` (`BusinessDataSource.getPermission` /
  `.setUserPermissions`), keyed by `(userId, businessId)`.
- **Appointments service** keeps its own local copy, written once when a
  business enables appointments
  (`PermissionsDataSource.initPermissions`, called from
  `EnableAppointmentsForBusinessImpl` with the caller's permission level
  fetched cross-service via `BusinessClient.getPermission`). It is not kept
  in sync with later changes to the business-service grant.

Both stores use the same `Int` encoding (`ObjectPermission.int`), so
`ObjectPermission.of(value)` and `.assert(...)` behave identically
regardless of which service's table backs the check.

This means promoting an employee to `Manager` only grants `EDIT` in the
**business** service. An employee never gets a row in the appointments
service's own permission table in the first place (only the business owner
does, once, when enabling appointments) — so a promoted employee still
fails `EDIT`/`READ` checks on appointments operations regardless of their
business-service role, until the appointments service is given an
equivalent promote/sync path.
