package com.book.auth.data.orm.table

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

internal object AuthenticationTable : UUIDTable("authentication") {
    val userId = uuid("user_id").uniqueIndex()
    val uuid = uuid("uuid").uniqueIndex()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}