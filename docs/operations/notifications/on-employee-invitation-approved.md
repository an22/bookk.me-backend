# React to employee invitation approval

Kafka topic `BusinessEvent.EmployeeInvitationApproved` → `NotificationEventHandler` → `SendNotification`

Produced by [Approve employee invitation](../business/approve-employee-invitation.md).
Notifies the inviter (not the newly-approved employee) that their
invitation was accepted. Same suppression rules as [employee invitation
created](on-employee-invitation-created.md): always allowed, channel
selection only.

```mermaid
flowchart TD
    Start([Consume BusinessEvent.EmployeeInvitationApproved]) --> Dedup{idempotencyKey already processed?}
    Dedup -- Yes --> Skip([Skip - already handled])
    Dedup -- No --> Send[SendNotification to=inviterUserId]
    Send --> GetSettings[NotificationSettingsDataSource.getByUserId inviterUserId]
    GetSettings -- not found --> Dlt([Sent to BusinessEvent.EmployeeInvitationApproved_dlt topic, logged - no auto-retry])
    GetSettings -- found --> Channels[For each enabled channel: senderMap.channel.send inviterUserId notification - always allowed]
    Channels --> Ack([Marked processed])
```
