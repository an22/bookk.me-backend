# Update business

`PUT /api/business/{id}` → `UpdateBusiness`

Partial update: every field on `BusinessUpdateModel` is nullable and only
non-null fields are applied. String fields are silently truncated to the
entity's max length rather than rejected. Publishes `BusinessEvent.Updated`
so other services (appointments' business snapshot, in particular) stay in
sync.

```mermaid
flowchart TD
    Start([PUT /api/business/id]) --> PathCheck{path id == body.id?}
    PathCheck -- No --> R400a([400 Bad Request])
    PathCheck -- Yes --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{permission >= EDIT?}
    Perm -- No --> R404([404 Error.OperationNotAllowed])
    Perm -- Yes --> ScheduleGiven{body.schedule present?}
    ScheduleGiven -- Yes --> WorkHours{any active day with empty workingTime?}
    WorkHours -- Yes --> R422a([422 BUSINESS_ACTIVE_DAY_WITHOUT_WORK_HOURS 200019])
    WorkHours -- No --> DayOffRange{any dayOff.start > end?}
    DayOffRange -- Yes --> R422b([422 BUSINESS_INVALID_DAY_OFF_RANGE 200020])
    DayOffRange -- No --> Truncate[Truncate name/description/currencyCode/address/socials to max length]
    ScheduleGiven -- No --> Truncate
    Truncate --> Update[BusinessDataSource.updateBusiness updatedModel updatedAt]
    Update --> Event[eventProducer.send BusinessEvent.Updated]
    Event --> R204([204 No Content])
```

**Consumed by:** `BusinessEvent.Updated` → [appointments: refresh the
cached business snapshot](../appointments/on-business-updated.md).
