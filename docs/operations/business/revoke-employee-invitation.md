# Revoke employee invitation

`POST /api/business/{businessId}/employee_invitation/{id}/revoke` → `RevokeEmployeeInvitation`

Only the business owner can revoke an invitation, and only while it is
still `PENDING` — this lets an owner invalidate a shared invite code
before it is redeemed (or before it expires on its own via the
`expireEmployeeInvitations` scheduled job, see [Scheduled (recurring)
jobs](../scheduled-jobs.md)). The caller is checked via `ObjectPermission`.
Revocation flips the invitation's status and clears its `code` column
(nullable, see [Invite employee](create-employee-invitation.md)) so the
code is freed for reuse; it does not touch `Employee` or
`BusinessPermissionsTable`, and no cross-service event is published.

```mermaid
flowchart TD
    Start([POST .../employee_invitation/id/revoke]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{caller permission >= OWNER?}
    Perm -- No --> R404a([404 Error.OperationNotAllowed])
    Perm -- Yes --> GetInvite[EmployeeInvitationDataSource.getInvitation businessId id]
    GetInvite -- not found --> R404b([404 Error.NotFound])
    GetInvite -- found --> Status{invitation.status == PENDING?}
    Status -- No --> R422([422 BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED 200017])
    Status -- Yes --> Revoke[EmployeeInvitationDataSource.revokeInvitation id, clears code]
    Revoke -- race lost, already processed --> R422
    Revoke -- ok --> R204([204 Invitation revoked])
```
