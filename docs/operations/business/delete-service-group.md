# Delete service group

`DELETE /api/business/{businessId}/service_group/{id}` → `DeleteServiceGroup`

Deletes the group and, per the route's own description, every service that
belongs to it (cascading delete happens at the datasource/table level).

```mermaid
flowchart TD
    Start([DELETE /api/business/businessId/service_group/id]) --> Auth{JWT valid?}
    Auth -- No --> R401([401 Unauthorized])
    Auth -- Yes --> Tx[[Begin transaction]]
    Tx --> Perm{caller SERVICES.delete?}
    Perm -- No --> R404([404 Error.OperationNotAllowed])
    Perm -- Yes --> Delete[ServiceDataSource.deleteServiceGroup id - cascades to member services]
    Delete --> R204([204 Service group deleted])
```
