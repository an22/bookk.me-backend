package com.book.user.data.orm

import com.book.core.data.database.createMigrationScriptFor
import com.book.user.data.di.userTables

fun main() {
    createMigrationScriptFor(
        referenceVersion = 0,
        targetVersion = 1,
        schemaName = "user",
        tables = userTables()
    )
}