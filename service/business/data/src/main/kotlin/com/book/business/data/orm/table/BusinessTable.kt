package com.book.business.data.orm.table

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object BusinessTable: UUIDTable("business") {
    val userId = uuid("user_id").uniqueIndex()
    val name = varchar("name", 512)
    val description = varchar("description", 512)
    val address = varchar("address", 512)
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val currency = varchar("currency", 3)
    val instagram = varchar("instagram", 255).nullable()
    val telegram = varchar("telegram", 255).nullable()
    val viber = varchar("viber", 255).nullable()
    val whatsapp = varchar("whatsapp", 255).nullable()
    val phone = varchar("phone", 255).nullable()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}