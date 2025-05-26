package com.book.business.data.orm

import com.book.business.data.orm.table.BusinessDashboardTable
import com.book.business.data.orm.table.BusinessTable
import com.book.core.data.database.createMigrationScriptFor
import org.jetbrains.exposed.sql.Table

fun main() {
    createMigrationScriptFor(
        referenceVersion = 0,
        targetVersion = 1,
        schemaName = "business",
        tables = tables()
    )
}

private fun tables(): Array<Table> {
    return arrayOf(
        BusinessTable,
        BusinessDashboardTable
    )
}