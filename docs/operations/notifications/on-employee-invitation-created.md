# React to employee invitation creation

Kafka topic `BusinessEvent.EmployeeInvitationCreated` → `NotificationEventHandler` → `SendNotification`

Produced by [Invite employee](../business/create-employee-invitation.md).
Notifies the invited user. Unlike appointment notifications, this type is
account-level and not user-suppressible via `appointmentEnabled` — it
still only reaches channels the user has individually enabled.

```mermaid
flowchart TD
    Start([Consume BusinessEvent.EmployeeInvitationCreated]) --> Dedup{idempotencyKey already processed?}
    Dedup -- Yes --> Skip([Skip - already handled])
    Dedup -- No --> Send[SendNotification to=invitedUserId]
    Send --> GetSettings[NotificationSettingsDataSource.getByUserId invitedUserId]
    GetSettings -- not found --> Dlt([Sent to BusinessEvent.EmployeeInvitationCreated_dlt topic, logged - no auto-retry])
    GetSettings -- found --> Channels[For each enabled channel: senderMap.channel.send invitedUserId notification - always allowed]
    Channels --> Ack([Marked processed])
```
