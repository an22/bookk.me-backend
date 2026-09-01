# Create appointment request

`POST /api/appointments/request` → `CreateAppointmentRequest`

A client submits an `AppointmentRequestDraft` carrying the signed
`offerToken` produced earlier by the business service's
`POST /api/service/quote` (`IssueServiceQuote`, read-only — not diagrammed
here) alongside only the requested slot and the ids of the employee and
services — no name, contact or pricing data travels from the client, and no
client id either: the caller never gets to assert whose client record a
booking attaches to, it is always the authenticated `userId`. A service id
may repeat in `draft.serviceIds` to request more than one count of it (e.g.
5 counts of service X); `IssueServiceQuote` signs the *count per distinct
service id* into `QuoteClaims.CLAIM_SERVICES`
(`QuoteClaims.encodeServiceCounts` — `"<serviceId>:<count>"` entries, not a
flat repeated list) rather than a plain id set. The token's claims (service
counts, business id) are cross-checked against the draft's own per-id
counts before anything else happens, so a stale or tampered quote — or a
request that changes how many of a service were asked for — is rejected up
front. The employee, client and services are then resolved from the
business service in a single call to `GetAppointmentBookingContext`
(`BusinessClient.getAppointmentBookingContext`, given `businessId`,
`employeeId`, the caller's `userId` and `serviceIds`) — this is the
operation's actual authorization gate for *identity*: an employee that no
longer exists in the business, or a service that has since been removed,
fails here even if the signature check passed. The client side of that
resolution is a get-or-create keyed on `(businessId, userId)`
(`ClientDataSource.getClientByUserId`, falling back to
`ClientDataSource.getOrCreateIntegratedClient` seeded from the caller's
profile via `UserClient.getUserById` on the very first booking with this
business) inside the same call — so a first-time client is provisioned
without appointments ever needing to know or store a client id itself, and
without a second round trip to business. Only once that context is
resolved is the full `AppointmentRequest` (with real `EmployeeSnapshot`,
`ClientSnapshot`, `ServiceSnapshot`s) built and its total price *and* total
duration compared against the token's frozen values — the quote signs both
(`QuoteClaims.CLAIM_TOTAL`, `CLAIM_DURATION`), so a service whose duration
changes after the quote was issued (even if its price didn't) is caught the
same way a price change is, instead of silently booking a
longer-or-shorter slot than the client agreed to. There is no business-permission check on
top of this, since the caller is the client booking with the business, not
a staff member. If the business has `automaticApproval` on, the request is
approved immediately by delegating into [Create appointment from a pending
request](create-appointment-from-request.md)'s shared verify/persist logic
instead of being stored as pending.

```mermaid
flowchart TD
    Start([POST /api/appointments/request]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> TokenCached{offerToken already used?}
    TokenCached -- Yes --> R422a([422 TOKEN_ALREADY_USED 300016])
    TokenCached -- No --> Verify[Verify offerToken signature - SERVICE_QUOTE]
    Verify -- invalid signature --> R422b([422 SERVICES_VALIDATION_FAILED 300014])
    Verify -- valid --> ServicesCheck{draft's per-service counts and businessId match token claims?}
    ServicesCheck -- No --> R400b([400 SERVICES_VALIDATION_FAILED 300014])
    ServicesCheck -- Yes --> Resolve[[BusinessClient.getAppointmentBookingContext businessId employeeId userId serviceIds]]
    Resolve --> EmptyListCheck{serviceIds empty?}
    EmptyListCheck -- Yes --> R422h([422 BUSINESS_QUOTE_EMPTY_SERVICE_LIST 200014 - unreachable here, ServicesCheck already rejects an empty list])
    EmptyListCheck -- No --> EmployeeLookup[EmployeeDataSource.getEmployee businessId employeeId]
    EmployeeLookup -- missing --> R404b([404 BUSINESS_EMPLOYEE_NOT_EXISTS 200024])
    EmployeeLookup -- found --> ClientLookup{ClientDataSource.getClientByUserId businessId userId}
    ClientLookup -- none yet --> ClientCreate[UserClient.getUserById userId then ClientDataSource.getOrCreateIntegratedClient]
    ClientLookup -- found --> ServicesResolve
    ClientCreate --> ServicesResolve[ServiceDataSource.getServicesByIds serviceIds]
    ServicesResolve -- service missing --> R422g([422 BUSINESS_QUOTE_SERVICE_NOT_FOUND 200013])
    ServicesResolve -- resolved --> BuildRequest[Build AppointmentRequest from resolved employee/client/services]
    BuildRequest --> PriceCheck{request.totalAmount == token.total?}
    PriceCheck -- No --> R400a([400 PRICE_CHANGED 300013])
    PriceCheck -- Yes --> DurationCheck{request.dateEnd - request.date == token.duration?}
    DurationCheck -- No --> R400c([400 DURATION_CHANGED 300017])
    DurationCheck -- Yes --> Tx[[Begin transaction]]
    Tx --> Settings[AppointmentSettingsDataSource.getForUpdate businessId]
    Settings -- not found --> R404a([404 Error.NotFound])
    Settings -- found --> AutoApproval{settings.automaticApproval?}
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
    Snapshot -- missing --> R404d([404 Error.NotFound - logged as data inconsistency])
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
