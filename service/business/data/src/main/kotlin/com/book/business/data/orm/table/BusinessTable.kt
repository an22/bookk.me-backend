package com.book.business.data.orm.table

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BusinessTable: LongIdTable("business") {
    val userId = long("user_id").uniqueIndex()
    val name = varchar("device_name", 512)
    val description = varchar("description", 512).nullable()
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val currency = varchar("currency", 3).nullable()
    val instagram = varchar("instagram", 255).nullable()
    val telegram = varchar("telegram", 255).nullable()
    val viber = varchar("viber", 255).nullable()
    val whatsapp = varchar("whatsapp", 255).nullable()
    val phone = varchar("phone", 255).nullable()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}