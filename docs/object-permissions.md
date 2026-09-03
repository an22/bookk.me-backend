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

### Managing your own resource on a `READ` grant

A second overload, `Int?.assert(permission, actorId, assigneeId)`, lets a
`READ`-level caller pass an `EDIT`-level check when the resource being
mutated is assigned to them: it passes when the grant already meets
`permission`, **or** when the grant is at least `READ` and `actorId ==
assigneeId`. Appointments uses this for an employee acting on their own
appointment/request — `CreateAppointmentImpl`, `UpdateAppointmentImpl`,
`CancelAppointmentImpl`, `CreateAppointmentRequestImpl`, and
`DeclineAppointmentRequestImpl` all pass the caller's `userId` as `actorId`
and the appointment/request's `employee.userId` as `assigneeId`. An `EDIT`/
`OWNER` holder (a manager or the business owner) is unaffected — they can
still act on anyone's appointment. A `READ` holder acting on an appointment
assigned to a different employee still gets `OperationNotAllowed`.

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
  - Create/update/cancel/decline an appointment or appointment request
    **assigned to themselves** (`CreateAppointmentImpl`,
    `UpdateAppointmentImpl`, `CancelAppointmentImpl`,
    `CreateAppointmentRequestImpl`, `DeclineAppointmentRequestImpl`) — see
    [Managing your own resource on a `READ`
    grant](#managing-your-own-resource-on-a-read-grant)
- **Business**
  - List clients (`GetClientsImpl`)

This is also the level granted automatically to an **employee** once their
they join the business (`JoinBusinessImpl`) or when an owner
demotes/sets them to the `Employee` role via the promote-employee route
(see [Roles](#roles-employee--manager) below).

### `EDIT` (2)

Can create, update, or cancel/delete the business's data, in addition to
everything `READ` allows.

- **Appointments**
  - Create, update/reschedule, cancel, or decline **any** employee's
    appointment or appointment request (`CreateAppointmentImpl`,
    `UpdateAppointmentImpl`, `CancelAppointmentImpl`,
    `CreateAppointmentRequestImpl`, `DeclineAppointmentRequestImpl`) — a
    `READ` holder can already do this for their own, see
    [above](#read-1)
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
| Employee who joined | `JoinBusinessImpl` → `businessDataSource.setUserPermissions(requestUserId, businessId, ObjectPermission.READ.int)` | `READ` |
| Employee promoted/demoted by the owner | `PromoteEmployeeImpl` → `businessDataSource.setUserPermissions(employee.userId, businessId, role.toPermission().int)` | `READ` or `EDIT`, per the request's `role` |

The first two rows only ever create a grant once, for a specific user; the
promote route is the only operation that changes an **existing** grant, and
only ever between `READ` and `EDIT` — it cannot grant or revoke `OWNER`.

Both of the latter two rows also publish `BusinessEvent.EmployeePermissionChanged`
(`employeeUserId`, `businessId`, `permission`), which the appointments
service consumes to keep its own copy of the grant in sync — see
[Storage](#storage).

## Storage

This is a modular monolith: each microservice persists its own copy of the
grant rather than sharing one table across services.

- **Business service** owns the source-of-truth grant, in
  `BusinessPermissionsTable` (`BusinessDataSource.getPermission` /
  `.setUserPermissions`), keyed by `(userId, businessId)`.
- **Appointments service** keeps its own local copy in
  `UserHasAppointmentPermissions` (`PermissionsDataSource`), both written via
  the same `PermissionsDataSource.setPermissions` (an upsert, keyed by
  `(userId, businessId)`). The business owner's row is written once, when
  the business enables appointments (called from
  `EnableAppointmentsForBusinessImpl` with the caller's permission level
  fetched cross-service via `BusinessClient.getPermission`). An employee's
  row is written/overwritten by `SyncEmployeePermission`, called from
  `AppointmentEventHandler` in reaction to
  `BusinessEvent.EmployeePermissionChanged` (fired by
  `JoinBusinessImpl` and `PromoteEmployeeImpl` — see [How
  grants are created](#how-grants-are-created)), and is a no-op if
  the business hasn't enabled appointments yet
  (`AppointmentSubscriptionDataSource.isBusinessEnabled`) — an employee
  granted or promoted before their business turns appointments on gets no
  row until their permission changes again after that point. See
  [React to an employee permission
  change](operations/appointments/on-employee-permission-changed.md) for
  the full flow.

Both stores use the same `Int` encoding (`ObjectPermission.int`), so
`ObjectPermission.of(value)` and `.assert(...)` behave identically
regardless of which service's table backs the check.
