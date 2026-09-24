# Set employee permission

`PUT /api/business/{businessId}/employee/{id}/permissions/{resource}` → `SetEmployeePermission`

Grants or revokes independent `view`/`update`/`delete` access to one
`BusinessResource` for one employee at a time — there is no fixed role
anymore. The caller needs `EMPLOYEES.update` to manage permissions at all,
and additionally cannot hand out more access to the target resource than
they themselves hold (`.covers()`) — you cannot delegate what you don't
have. The `Employee` row itself is unchanged. See [Resource
permissions](../../object-permissions.md) for the full model.

```mermaid
flowchart TD
    Start([PUT /api/business/businessId/employee/id/permissions/resource]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> PermCheck{caller EMPLOYEES.update?}
    PermCheck -- No --> R404a([404 Error.OperationNotAllowed])
    PermCheck -- Yes --> Lookup[EmployeeDataSource.getEmployee businessId, id]
    Lookup --> Found{employee found?}
    Found -- No --> R404b([404 Error.NotFound])
    Found -- Yes --> OwnGrant[BusinessPermissionDataSource.getPermission caller, businessId, resource]
    OwnGrant --> Covers{caller's own grant covers the requested permission?}
    Covers -- No --> R422([422 BUSINESS_INSUFFICIENT_GRANT_PERMISSION 200027])
    Covers -- Yes --> Set[BusinessPermissionDataSource.setPermission employee.userId, businessId, resource, permission]
    Set --> Aggregate[BusinessPermissionDataSource.getPermissions employee.userId, businessId]
    Aggregate --> Event[eventProducer.send BusinessEvent.EmployeePermissionsChanged]
    Event --> R200([200 employee's updated BusinessPermissions])
```

**Consumed by:** `BusinessEvent.EmployeePermissionsChanged` → [appointments:
sync employee permissions](../appointments/on-employee-permissions-changed.md).
