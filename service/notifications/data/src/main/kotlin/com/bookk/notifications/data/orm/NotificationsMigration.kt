package com.bookk.notifications.data.orm

import com.bookk.core.data.database.createMigrationScriptFor
import com.bookk.notifications.data.orm.table.DeviceTable
import com.bookk.notifications.data.orm.table.NotificationSettingsTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table

fun main() {
    runBlocking {
        createMigrationScriptFor(
            referenceVersion = 1,
            targetVersion = 2,
            schemaName = "notifications",
            tables = tables()
        )
    }
}

private fun tables(): Array<Table> = arrayOf(DeviceTable, NotificationSettingsTable)
