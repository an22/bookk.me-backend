package com.book.user.data.orm

import com.book.core.data.database.createMigrationScriptFor
import com.book.user.data.orm.table.UserTable
import org.jetbrains.exposed.sql.Table

fun main() {
    createMigrationScriptFor(
        referenceVersion = 0,
        targetVersion = 1,
        schemaName = "user",
        tables = userTables()
    )
}

private fun userTables(): Array<Table> {
    return arrayOf(
        UserTable
    )
}