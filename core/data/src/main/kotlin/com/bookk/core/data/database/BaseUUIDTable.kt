package com.bookk.core.data.database

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

abstract class BaseUUIDTable(name: String) : UUIDTable(name) {
    val createdAt = timestamp("createdAt").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updatedAt").nullable()
}