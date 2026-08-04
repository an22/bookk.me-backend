package com.bookk.auth.data.orm

import com.bookk.auth.data.orm.table.AuthDeviceTable
import com.bookk.auth.data.orm.table.AuthenticationTable
import com.bookk.auth.data.orm.table.PasskeyCredentialTable
import com.bookk.core.data.database.createMigrationScriptFor
import kotlinx.coroutines.runBlocking
import library.signing.impl.orm.table.signingKeyTables
import org.jetbrains.exposed.v1.core.Table

fun main() {
    runBlocking {
        createMigrationScriptFor(
            referenceVersion = 0,
            targetVersion = 1,
            schemaName = "authorization",
            tables = authTables() + signingKeyTables()
        )
    }
}

private fun authTables(): Array<Table> = arrayOf(
    AuthDeviceTable,
    AuthenticationTable,
    PasskeyCredentialTable
)