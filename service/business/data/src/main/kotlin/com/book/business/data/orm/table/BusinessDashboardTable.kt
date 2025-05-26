package com.book.business.data.orm.table

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BusinessDashboardTable : LongIdTable("business_dashboard") {
    val userId = long("user_id").uniqueIndex()
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE).index()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}