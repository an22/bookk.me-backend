package com.bookk.appointments.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.core.ReferenceOption

object SettingsTable: BaseUUIDTable("AppointmentSettings") {
    val businessId = reference("business_id", BusinessHasAppointments, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val timeZone = varchar("time_zone", 256)
    val workingDays = byte("working_days")
    val inBetweenBreakInMinutes = integer("in_between_break_in_minutes")
    val appointmentNote = varchar("appointment_note", 2048)
}