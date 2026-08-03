package com.bookk.business.data.orm

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.ClientTable
import com.bookk.business.data.orm.table.EmployeeCanProvideServiceTable
import com.bookk.business.data.orm.table.EmployeeTable
import com.bookk.business.data.orm.table.ServiceGroupTable
import com.bookk.business.data.orm.table.ServiceTable
import com.bookk.core.data.database.createMigrationScriptFor
import kotlinx.coroutines.runBlocking
import library.signing.impl.orm.table.signingKeyTables
import org.jetbrains.exposed.v1.core.Table

fun main() {
    runBlocking {
        createMigrationScriptFor(
            referenceVersion = 8,
            targetVersion = 9,
            schemaName = "business",
            tables = tables() + signingKeyTables()
        )
    }
}

private fun tables(): Array<Table> {
    return arrayOf(
        BusinessTable,
        BusinessDashboardTable,
        ClientTable,
        BusinessPermissionsTable,
        ServiceTable,
        ServiceGroupTable,
        EmployeeTable,
        EmployeeCanProvideServiceTable
    )
}