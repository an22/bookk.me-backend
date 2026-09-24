# Set employee permissions

`PUT /api/business/{businessId}/employee/{id}/permissions` → `SetEmployeePermissions`

Grants or revokes independent `view`/`update`/`delete` access to one or more
`BusinessResource`s for one employee in a single request. The body,
`EmployeePermissionsRequest`, is a `Map<BusinessResource, ResourcePermission>`:
resources not listed keep their current grants. The caller needs
`EMPLOYEES.update` to manage permissions at all, and additionally cannot
hand out more access to any listed resource than they themselves hold
(`.covers()`) — you cannot delegate what you don't have. The check is
all-or-nothing: if a single grant exceeds the caller's own, nothing is
written and no event is sent. An empty map is a no-op that returns the
employee unchanged. The `Employee` row itself is unchanged; the response is
the `Employee` with its merged `permissions`, computed in memory as
`employee.permissions.with(grants)` rather than re-read. See [Resource
permissions](../../object-permissions.md) for the full model.

```mermaid
flowchart TD
    Start([PUT /api/business/businessId/employee/id/permissions]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> PermCheck{caller EMPLOYEES.update?}
    PermCheck -- No --> R404a([404 Error.OperationNotAllowed])
    PermCheck -- Yes --> Lookup[EmployeeDataSource.getEmployee businessId, id]
    Lookup --> Found{employee found?}
    Found -- No --> R404b([404 Error.NotFound])
    Found -- Yes --> Empty{grants empty?}
    Empty -- Yes --> R200a([200 Employee unchanged])
    Empty -- No --> OwnGrants[BusinessPermissionDataSource.getPermissions caller, businessId]
    OwnGrants --> Covers{caller's grant covers every requested resource's permission?}
    Covers -- No --> R422([422 BUSINESS_INSUFFICIENT_GRANT_PERMISSION 200027])
    Covers -- Yes --> Set[BusinessPermissionDataSource.setPermissions employee, grants - one batch upsert keyed by employee_id/user_id/business_id]
    Set --> Merge[employee.copy permissions = employee.permissions.with grants]
    Merge --> Event[eventProducer.send BusinessEvent.EmployeePermissionsChanged updated.permissions - once per request]
    Event --> R200([200 Employee with updated permissions])
```

**Consumed by:** `BusinessEvent.EmployeePermissionsChanged` → [appointments:
sync employee permissions](../appointments/on-employee-permissions-changed.md).
