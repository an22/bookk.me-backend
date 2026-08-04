package com.bookk.user.data.orm.table

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.time.Clock.System

object UserTable : UUIDTable("profile") {
    val name = varchar("name", 255)
    val lastName = varchar("last_name", 255)
    val email = varchar("email", 320).uniqueIndex()
    val phone = varchar("phone", 512).nullable()
    val createdAt = timestamp("created_at").clientDefault { System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}