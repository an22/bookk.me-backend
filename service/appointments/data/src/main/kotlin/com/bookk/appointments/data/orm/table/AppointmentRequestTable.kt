package com.bookk.appointments.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object AppointmentRequestTable : BaseUUIDTable("appointment_request") {
    val userId = uuid("user_id")
    val businessId = uuid("business_id")
    val clientId = uuid("client_id")
    val clientName = varchar("client_name", 1024)
    val clientPhone = varchar("client_phone", 24).nullable()
    val clientEmail = varchar("client_email", 512).nullable()
    val serviceId = uuid("service_id")
    val serviceName = varchar("service_name", 1024)
    val serviceGroupId = uuid("service_group_id")
    val priceCurrency = varchar("price_currency", 3)
    val priceUnscaled = long("price_unscaled")
    val priceScale = integer("price_scale")
    val durationMinutes = long("duration_minutes")
    val date = timestamp("date")
    val note = varchar("note",2048)
}