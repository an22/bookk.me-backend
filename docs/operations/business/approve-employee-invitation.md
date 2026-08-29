# Approve employee invitation

`POST /api/business/{businessId}/employee_invitation/{id}/approve` → `ApproveEmployeeInvitation`

Only the invited email's own user can approve their invitation — the
caller identity is cross-checked against the invitation's `email` via the
user service, and a mismatch is reported as the same 404 as a missing
invitation so an attacker can't distinguish "not invited" from "invited
someone else". Approval creates the `Employee` row and grants baseline
`READ` permission on the business.

```mermaid
flowchart TD
    Start([POST .../employee_invitation/id/approve]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> GetInvite[EmployeeInvitationDataSource.getInvitation businessId id]
    GetInvite -- not found --> R404a([404 Error.NotFound])
    GetInvite -- found --> GetUser[UserClient.getUserById requestUserId]
    GetUser -- error --> RErr([Propagate user-service error])
    GetUser -- ok --> EmailMatch{invitation.email == requestUser.email?}
    EmailMatch -- No --> R404b([404 Error.OperationNotAllowed])
    EmailMatch -- Yes --> Status{invitation.status == PENDING?}
    Status -- No --> R422a([422 BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED 200017])
    Status -- Yes --> AlreadyEmployee{employeeDataSource.getEmployeeByUserId already an employee?}
    AlreadyEmployee -- Yes --> R422b([422 BUSINESS_EMPLOYEE_EXISTS 200018])
    AlreadyEmployee -- No --> Approve[EmployeeInvitationDataSource.approveInvitation id]
    Approve -- race lost, already processed --> R422a
    Approve -- ok --> GetBiz[BusinessDataSource.getBusinessById businessId]
    GetBiz -- not found --> R404c([404 Error.NotFound])
    GetBiz -- found --> CreateEmployee[EmployeeDataSource.createEmployee from requestUser]
    CreateEmployee --> SetPerm[BusinessDataSource.setUserPermissions requestUserId businessId READ]
    SetPerm --> Event[eventProducer.send BusinessEvent.EmployeeInvitationApproved]
    Event --> PermEvent[eventProducer.send BusinessEvent.EmployeePermissionChanged READ]
    PermEvent --> R200([200 Created Employee])
```

**Consumed by:** `BusinessEvent.EmployeeInvitationApproved` → [notifications:
notify the inviter](../notifications/on-employee-invitation-approved.md).
`BusinessEvent.EmployeePermissionChanged` → [appointments: sync employee
permission](../appointments/on-employee-permission-changed.md).
