# Join business

`POST /api/employee_invitation/redeem` → `JoinBusiness`

Any authenticated user can join a business by submitting the invite code
shared with them by the business owner — there is no per-user targeting,
whoever holds a `PENDING` code can join. Joining creates the `Employee`
row for the calling user and grants baseline `READ` permission on the
business.

```mermaid
flowchart TD
    Start([POST /api/employee_invitation/redeem]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> GetInvite[EmployeeInvitationDataSource.getInvitationByCode code]
    GetInvite -- not found --> R404a([404 Error.NotFound])
    GetInvite -- found --> Status{invitation.status == PENDING?}
    Status -- No --> R422a([422 BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED 200017])
    Status -- Yes --> AlreadyEmployee{employeeDataSource.getEmployeeByUserId already an employee?}
    AlreadyEmployee -- Yes --> R422b([422 BUSINESS_EMPLOYEE_EXISTS 200018])
    AlreadyEmployee -- No --> Redeem[EmployeeInvitationDataSource.redeemInvitation id]
    Redeem -- race lost, already processed --> R422a
    Redeem -- ok --> GetUser[UserClient.getUserById requestUserId]
    GetUser -- error --> RErr([Propagate user-service error])
    GetUser -- ok --> GetBiz[BusinessDataSource.getBusinessById businessId]
    GetBiz -- not found --> R404b([404 Error.NotFound])
    GetBiz -- found --> CreateEmployee[EmployeeDataSource.createEmployee from requestUser]
    CreateEmployee --> SetPerm[BusinessDataSource.setUserPermissions requestUserId businessId READ]
    SetPerm --> Event[eventProducer.send BusinessEvent.EmployeeInvitationRedeemed]
    Event --> PermEvent[eventProducer.send BusinessEvent.EmployeePermissionChanged READ]
    PermEvent --> R200([200 Created Employee])
```

**Consumed by:** `BusinessEvent.EmployeeInvitationRedeemed` → [notifications:
notify the inviter](../notifications/on-employee-invitation-redeemed.md).
`BusinessEvent.EmployeePermissionChanged` → [appointments: sync employee
permission](../appointments/on-employee-permission-changed.md).
