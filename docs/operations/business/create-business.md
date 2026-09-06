# Create business

`POST /api/business` → `CreateBusiness`

A user owns at most one business (`BusinessTable.userId` is a unique
index), so the existence check and the unique-constraint fallback both
guard the same invariant.

The owner is also inserted as an `EmployeeTable` row for the new business,
snapshotting their name/lastName/phone/email from `UserClient.getUserById`
at creation time. This is what makes `GetUserBusinesses` return owned and
employed-at businesses through the same query, and it is also why
`JoinBusiness` rejects the owner redeeming an invite code for their own
business — `EmployeeDataSource.getEmployeeByUserId` already finds their
owner-employee row and `JoinBusiness.Error.EmployeeExist` fires.

```mermaid
flowchart TD
    Start([POST /api/business]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> NameCheck{NameValidator.isValid name?}
    NameCheck -- No --> R422a([422 BUSINESS_NAME_VALIDATION_ERROR 200002])
    NameCheck -- Yes --> Tx[[Begin transaction]]
    Tx --> Exists{businessDataSource.isBusinessExist userId?}
    Exists -- Yes --> R422b([422 BUSINESS_ALREADY_EXIST 200001])
    Exists -- No --> Create[BusinessDataSource.createBusiness userId name currencyCode timeZone]
    Create --> SetPerm[BusinessPermissionDataSource.setPermission userId business.id resource FULL, for every BusinessResource]
    SetPerm --> ResolveOwner[UserClient.getUserById userId]
    ResolveOwner --> CreateEmployee[EmployeeDataSource.createEmployee owner as employee of business.id]
    CreateEmployee --> Constraint{Unique constraint violated?}
    Constraint -- Yes --> R422b
    Constraint -- No --> R200([200 Created Business])
```
