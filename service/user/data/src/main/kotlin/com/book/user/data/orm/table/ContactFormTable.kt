package com.book.user.data.orm.table

import kotlinx.datetime.Clock.System
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ContactFormTable: LongIdTable("contact_form") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE).index()
    val text = text("text")
    val usageLogs = text("usage_logs").nullable()
    val createdAt = timestamp("created_at").clientDefault { System.now() }
    val updatedAt = timestamp("updated_at")
}