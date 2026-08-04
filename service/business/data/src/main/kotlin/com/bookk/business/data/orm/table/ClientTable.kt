package com.bookk.business.data.orm.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

internal object ClientTable: UUIDTable("client") {
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE).index()
    val name = varchar("name", 512)
    val lastName = varchar("lastname", 512)
    val phone = varchar("phone", 512).index()
    val email = varchar("email", 512).index()
    val userId = uuid("user_id").nullable().index()
    val sourceUpdatedAt = timestamp("source_updated_at").nullable()
}