package com.book.auth.data.orm.table

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

internal object AuthenticationTable : LongIdTable("authentication") {
    val userId = long("user_id").uniqueIndex()
    val email = varchar("email", 320)
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at")
}