# Join business

`POST /api/employee_invitation/redeem` → `JoinBusiness`

Any authenticated user can join a business by submitting the invite code
shared with them by the business owner — there is no per-user targeting,
whoever holds a `PENDING` code can join. Joining creates the `Employee`
row for the calling user and grants baseline `READ` permission on the
business. `redeemInvitation` also clears the invitation's `code` column
(nullable, see [Invite employee](create-employee-invitation.md)) so it can
be handed out again to a future invitee. Because the code is gone as soon
as it's consumed, resubmitting an already-redeemed code later looks
identical to submitting a code that never existed — `getInvitationByCode`
simply won't find it, so the caller gets 404 rather than the 422
`ALREADY_PROCESSED` error. That error is still reached for the genuine
race: two requests fetching the same still-`PENDING` invitation before
either commits, where the loser's `redeemInvitation` call matches zero
rows.

```mermaid
flowchart TD
    Start([POST /api/employee_invitation/redeem]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> GetInvite[EmployeeInvitationDataSource.getInvitationByCode code]
    GetInvite -- not found or code already cleared --> R404a([404 Error.NotFound])
    GetInvite -- found --> Status{invitation.status == PENDING?}
    Status -- No --> R422a([422 BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED 200017])
    Status -- Yes --> AlreadyEmployee{employeeDataSource.getEmployeeByUserId already an employee?}
    AlreadyEmployee -- Yes --> R422b([422 BUSINESS_EMPLOYEE_EXISTS 200018])
    AlreadyEmployee -- No --> Redeem[EmployeeInvitationDataSource.redeemInvitation id, clears code]
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
