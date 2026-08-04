package com.bookk.user.data.orm

import com.bookk.core.data.database.createMigrationScriptFor
import com.bookk.user.data.orm.table.ContactFormTable
import com.bookk.user.data.orm.table.UserTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Table

fun main() {
    runBlocking {
        createMigrationScriptFor(
            referenceVersion = 0,
            targetVersion = 1,
            schemaName = "user",
            tables = userTables()
        )
    }
}

private fun userTables(): Array<Table> {
    return arrayOf(
        UserTable,
        ContactFormTable
    )
}