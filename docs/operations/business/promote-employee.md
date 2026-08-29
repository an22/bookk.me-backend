# Promote employee

`POST /api/business/{businessId}/employee/{id}/promote` → `PromoteEmployee`

Only the business owner can change an employee's permission level. `role`
in the request body is one of `EMPLOYEE`/`MANAGER`, mapped to
`ObjectPermission.READ`/`ObjectPermission.EDIT` and written to
`BusinessPermissionsTable` for the employee's `userId`; the `Employee` row
itself is unchanged. See [Object permissions](../../object-permissions.md)
for what each level allows.

```mermaid
flowchart TD
    Start([POST /api/business/businessId/employee/id/promote]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{caller permission >= OWNER?}
    Perm -- No --> R404a([404 Error.OperationNotAllowed])
    Perm -- Yes --> Lookup[EmployeeDataSource.getEmployee businessId, id]
    Lookup --> Found{employee found?}
    Found -- No --> R404b([404 Error.NotFound])
    Found -- Yes --> Map[role.toPermission -> READ or EDIT]
    Map --> Set[BusinessDataSource.setUserPermissions employee.userId, businessId, permission]
    Set --> Event[eventProducer.send BusinessEvent.EmployeePermissionChanged]
    Event --> R204([204 Employee promoted])
```

**Consumed by:** `BusinessEvent.EmployeePermissionChanged` → [appointments:
sync employee permission](../appointments/on-employee-permission-changed.md).
