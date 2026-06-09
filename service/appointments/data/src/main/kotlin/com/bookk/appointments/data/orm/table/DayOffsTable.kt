package com.bookk.appointments.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.date

object DayOffsTable: BaseUUIDTable("appointment_day_offs") {
    val settingsId = reference("settings_id", SettingsTable, onDelete = ReferenceOption.CASCADE).index()
    val date = date("date")
}