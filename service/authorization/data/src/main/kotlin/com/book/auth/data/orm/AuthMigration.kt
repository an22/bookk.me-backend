package com.book.auth.data.orm

import com.book.auth.data.orm.table.AuthDeviceTable
import com.book.auth.data.orm.table.AuthToHandleTable
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.data.orm.table.PasskeyCredentialTable
import com.book.core.data.database.createMigrationScriptFor
import org.jetbrains.exposed.sql.Table

fun main() {
    createMigrationScriptFor(
        referenceVersion = 0,
        targetVersion = 1,
        schemaName = "authorization",
        tables = authTables()
    )
}

private fun authTables(): Array<Table> = arrayOf(
    AuthDeviceTable,
    AuthenticationTable,
    PasskeyCredentialTable,
    AuthToHandleTable
)