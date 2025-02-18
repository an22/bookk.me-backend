package com.book.user.data.orm.table

import kotlinx.datetime.Clock.System
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : LongIdTable("profile") {
    val name = varchar("name", 255)
    val lastName = varchar("last_name", 255)
    val email = varchar("email", 320).uniqueIndex()
    val createdAt = timestamp("created_at").clientDefault { System.now() }
    val updatedAt = timestamp("updated_at")
}