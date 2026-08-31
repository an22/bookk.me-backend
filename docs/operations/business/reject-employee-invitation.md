# Reject employee invitation

`POST /api/business/{businessId}/employee_invitation/{id}/reject` → `RejectEmployeeInvitation`

Only the invited email's own user can reject their invitation — the caller
identity is cross-checked against the invitation's `email` via the user
service, and a mismatch is reported as the same 404 as a missing invitation
so an attacker can't distinguish "not invited" from "invited someone
else". Rejection only flips the invitation's status; it does not touch
`Employee` or `BusinessPermissionsTable`, and no cross-service event is
published.

```mermaid
flowchart TD
    Start([POST .../employee_invitation/id/reject]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> GetInvite[EmployeeInvitationDataSource.getInvitation businessId id]
    GetInvite -- not found --> R404a([404 Error.NotFound])
    GetInvite -- found --> GetUser[UserClient.getUserById requestUserId]
    GetUser -- error --> RErr([Propagate user-service error])
    GetUser -- ok --> EmailMatch{invitation.email == requestUser.email?}
    EmailMatch -- No --> R404b([404 Error.OperationNotAllowed])
    EmailMatch -- Yes --> Status{invitation.status == PENDING?}
    Status -- No --> R422([422 BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED 200017])
    Status -- Yes --> Reject[EmployeeInvitationDataSource.rejectInvitation id]
    Reject -- race lost, already processed --> R422
    Reject -- ok --> R204([204 Invitation rejected])
```
