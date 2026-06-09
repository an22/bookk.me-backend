package com.bookk.appointments.data.orm

import com.bookk.appointments.data.orm.table.AppointmentRequestTable
import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.data.orm.table.BusinessHasAppointments
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.data.orm.table.UserHasAppointmentPermissions
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.core.data.database.createMigrationScriptFor
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table

fun main() {
    runBlocking {
        createMigrationScriptFor(
            referenceVersion = 0,
            targetVersion = 1,
            schemaName = "appointments",
            tables = tables()
        )
    }
}

private fun tables(): Array<Table> = arrayOf(
    AppointmentTable,
    BusinessHasAppointments,
    DayOffsTable,
    AppointmentRequestTable,
    SettingsTable,
    UserHasAppointmentPermissions,
    WorkingHoursTable
)