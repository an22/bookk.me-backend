# Invite employee

`POST /api/business/{businessId}/employee_invitation` → `CreateEmployeeInvitation`

Only an `OWNER` can invite (stricter than the usual `EDIT` bar elsewhere in
this service). The invite-email lookup against the user service is
best-effort: if the invited email has no matching user yet, the
`EmployeeInvitationCreated` event is simply skipped rather than failing the
whole request — the invitation row still exists for when they sign up.

```mermaid
flowchart TD
    Start([POST /api/business/businessId/employee_invitation]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> EmailCheck{EmailValidator.isValid email?}
    EmailCheck -- No --> R422a([422 BUSINESS_EMPLOYEE_INVITATION_VALIDATION_ERROR 200016])
    EmailCheck -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{permission >= OWNER?}
    Perm -- No --> R404a([404 Error.OperationNotAllowed])
    Perm -- Yes --> EmployeeExists{employeeDataSource.getEmployeeByEmail already an employee?}
    EmployeeExists -- Yes --> R422b([422 BUSINESS_EMPLOYEE_EXISTS 200018])
    EmployeeExists -- No --> GetBiz[BusinessDataSource.getBusinessById businessId]
    GetBiz -- not found --> R404b([404 Error.NotFound])
    GetBiz -- found --> CreateInvite[EmployeeInvitationDataSource.createInvitation invitation]
    CreateInvite --> Constraint{Unique constraint violated - pending invite exists?}
    Constraint -- Yes --> R422c([422 BUSINESS_EMPLOYEE_INVITATION_EXISTS 200015])
    Constraint -- No --> LookupUser[UserClient.getUserByEmail email]
    LookupUser -- found --> Event[eventProducer.send BusinessEvent.EmployeeInvitationCreated]
    LookupUser -- not found --> Skip[Skip event - invitation still persisted]
    Event --> R200([200 Created EmployeeInvitation])
    Skip --> R200
```

**Consumed by:** `BusinessEvent.EmployeeInvitationCreated` → [notifications:
notify the invited user](../notifications/on-employee-invitation-created.md)
— only fires when the email lookup above succeeds.
