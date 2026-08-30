# Create appointment request

`POST /api/appointments/request` → `CreateAppointmentRequest`

A client submits an `AppointmentOffer` produced earlier by the business
service's `POST /api/service/quote` (`IssueQuote`, read-only — not
diagrammed here): a signed `offerToken` plus the
requested slot. The token is verified and its claims (services, total,
business id) are cross-checked against the request body before anything is
persisted, so a stale or tampered quote is rejected up front. If the
business has `automaticApproval` on, the request is approved immediately by
delegating into [Create appointment from a pending
request](create-appointment-from-request.md)'s shared verify/persist logic
instead of being stored as pending. A `READ`-level employee can create a
request assigned to themselves; see [Managing your own resource on a
`READ`
grant](../../object-permissions.md#managing-your-own-resource-on-a-read-grant).

```mermaid
flowchart TD
    Start([POST /api/appointments/request]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> TokenCached{offerToken already used?}
    TokenCached -- Yes --> R422a([422 TOKEN_ALREADY_USED 300016])
    TokenCached -- No --> Verify[Verify offerToken signature - SERVICE_QUOTE]
    Verify -- invalid signature --> R422b([422 SERVICES_VALIDATION_FAILED 300014])
    Verify -- valid --> PriceCheck{request.totalAmount == token.total?}
    PriceCheck -- No --> R400a([400 PRICE_CHANGED 300013])
    PriceCheck -- Yes --> ServicesCheck{request.services and businessId match token claims?}
    ServicesCheck -- No --> R400b([400 SERVICES_VALIDATION_FAILED 300014])
    ServicesCheck -- Yes --> Tx[[Begin transaction]]
    Tx --> Settings[AppointmentSettingsDataSource.getForUpdate businessId]
    Settings -- not found --> R404a([404 Error.NotFound])
    Settings -- found --> Perm{permission >= EDIT, or permission >= READ and request.employee.userId == userId?}
    Perm -- No --> R404b([404 Error.OperationNotAllowed])
    Perm -- Yes --> AutoApproval{settings.automaticApproval?}
    AutoApproval -- Yes --> Delegate[Delegate to CreateAppointment userId request]
    Delegate --> DelegateFlow[[See create-appointment-from-request.md verify and persist flow]]
    DelegateFlow --> R200a([200 Appointment - created directly])
    AutoApproval -- No --> PastCheck{request.date < now?}
    PastCheck -- Yes --> R422c([422 DATE_IN_PAST 300012])
    PastCheck -- No --> WorkdayCheck{date within business workday?}
    WorkdayCheck -- No --> R422d([422 DATE_NOT_ALLOWED 300003])
    WorkdayCheck -- Yes --> WorktimeCheck{slot within worktime?}
    WorktimeCheck -- No --> R422e([422 TIME_NOT_ALLOWED 300002])
    WorktimeCheck -- Yes --> OverlapRequests{overlaps existing request?}
    OverlapRequests -- Yes --> R422f([422 REQUEST_EXISTS 300001])
    OverlapRequests -- No --> OverlapAppointments{overlaps existing appointment?}
    OverlapAppointments -- Yes --> R422f
    OverlapAppointments -- No --> Create[AppointmentRequestDataSource.create request]
    Create --> Snapshot[AppointmentSubscriptionDataSource.getBusinessSnapshot businessId]
    Snapshot -- missing --> R404c([404 Error.NotFound - logged as data inconsistency])
    Snapshot -- found --> Event[eventProducer.send AppointmentEvent.RequestCreated]
    Event --> CacheToken[requestDataSource.cacheOfferToken offerToken]
    CacheToken --> R204([204 No Content])
```

**Consumed by:** `AppointmentEvent.RequestCreated` → [notifications: notify the
employee](../notifications/on-appointment-request-created.md). The
`automaticApproval` branch delegates into [Create appointment from a
pending request](create-appointment-from-request.md)'s shared logic and
sends `AppointmentEvent.RequestApproved` instead — see [notifications:
notify the client of
approval](../notifications/on-appointment-request-approved.md).
