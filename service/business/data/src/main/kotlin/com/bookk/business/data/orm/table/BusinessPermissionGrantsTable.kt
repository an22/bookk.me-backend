package com.bookk.business.data.orm.table

import com.bookk.business.domain.api.business.entity.BusinessResource
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object BusinessPermissionGrantsTable : UuidTable("business_permission_grants") {
    val userId = uuid("user_id")
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE)
    val resource = enumeration("resource", BusinessResource::class)
    val canView = bool("can_view")
    val canUpdate = bool("can_update")
    val canDelete = bool("can_delete")

    init {
        index(isUnique = true, userId, businessId, resource)
    }
}
