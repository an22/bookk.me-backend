package com.bookk.notifications.data.orm

import com.bookk.core.data.database.createMigrationScriptFor
import com.bookk.notifications.data.orm.table.DeviceTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table

fun main() {
    runBlocking {
        createMigrationScriptFor(
            referenceVersion = 0,
            targetVersion = 1,
            schemaName = "notifications",
            tables = tables()
        )
    }
}

private fun tables(): Array<Table> = arrayOf(DeviceTable)
