package com.bookk.business.data.orm.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object BusinessPermissionsTable : UuidTable("business_permissions") {
    val userId = uuid("user_id")
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE)
    val permission = integer("permission")

    init {
        index(isUnique = true, userId, businessId)
    }
}