package com.book.core.data.database

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.core.Schema
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.migration.MigrationUtils
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.io.File

private const val URL = "jdbc:mariadb://127.0.0.1:3308"
private const val R2DBC_URL = "r2dbc:mariadb://127.0.0.1:3308"
private const val USER = "root"
private const val PASS = "migration"
private const val DRIVER = "org.mariadb.jdbc.Driver"

@OptIn(ExperimentalDatabaseMigrationApi::class)
suspend fun createMigrationScriptFor(
    referenceVersion: Int,
    targetVersion: Int,
    schemaName: String,
    tables: Array<Table>
) {
    val migrationDir = File("src/main/resources/db/migration/${schemaName}")
    if (!migrationDir.exists()) {
        migrationDir.mkdirs()
    }
    moveDBWithVersion(referenceVersion, migrationDir.path, schemaName)
    suspendTransaction {
        SchemaUtils.setSchema(Schema(schemaName))
        val file = MigrationUtils.generateMigrationScript(
            tables = tables,
            scriptDirectory = "${migrationDir.path}",
            scriptName = "V${targetVersion}__migration_script",
            withLogs = true
        )
        Flyway.configure()
            .dataSource(URL, USER, PASS)
            .driver(DRIVER)
            .baselineOnMigrate(true)
            .defaultSchema(schemaName)
            .locations("filesystem:${file.parent}")
            .target("$targetVersion")
            .load()
            .migrate()
    }
}

private suspend fun moveDBWithVersion(
    schemaVersion: Int,
    migrationsFolderPath: String,
    schemaName: String
) {
    R2dbcDatabase.connect(
        url = R2DBC_URL,
        user = USER,
        password = PASS
    )
    suspendTransaction {
        SchemaUtils.dropSchema(Schema(schemaName))
    }
    if (schemaVersion != 0) {
        Flyway.configure()
            .dataSource(URL, USER, PASS)
            .driver(DRIVER)
            .baselineOnMigrate(true)
            .defaultSchema(schemaName)
            .createSchemas(true)
            .locations("${migrationsFolderPath}/$schemaName")
            .target("$schemaVersion")
            .load()
            .migrate()
    } else {
        suspendTransaction {
            SchemaUtils.createSchema(Schema(schemaName))
            SchemaUtils.setSchema(Schema(schemaName))
        }
    }
}