package com.book.auth.data.orm

import com.book.auth.data.di.authTables
import com.book.core.data.database.createMigrationScriptFor

fun main() {
    createMigrationScriptFor(
        referenceVersion = 0,
        targetVersion = 1,
        schemaName = "auth",
        tables = authTables()
    )
}