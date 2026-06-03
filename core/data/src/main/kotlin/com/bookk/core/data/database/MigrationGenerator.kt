package com.bookk.core.data.database

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import java.io.File

private const val URL = "jdbc:mariadb://127.0.0.1:3308"
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
    moveDBToVersion(
        flyway = createFlywayForVersion(
            schemaName,
            "db/migration/${schemaName}",
            referenceVersion
        ),
        schemaVersion = referenceVersion,
        schemaName = schemaName
    )
    val fileSystemTarget = File("src/main/resources/db/migration/${schemaName}")
    val file = suspendTransaction {
        MigrationUtils.generateMigrationScript(
            tables = tables,
            scriptDirectory = "${fileSystemTarget.path}",
            scriptName = "V${targetVersion}__migration_script",
            withLogs = true
        )
    }
    createFlywayForVersion(
        schemaName = schemaName,
        location = "filesystem:${file.parent}",
        version = targetVersion
    ).migrate()
}

private fun createFlywayForVersion(
    schemaName: String,
    location: String,
    version: Int
): Flyway = Flyway.configure()
    .dataSource(URL, USER, PASS)
    .driver(DRIVER)
    .baselineVersion("0")
    .baselineOnMigrate(true)
    .defaultSchema(schemaName)
    .createSchemas(true)
    .failOnMissingLocations(true)
    .locations(location)
    .target("$version")
    .cleanDisabled(false)
    .load()

private fun moveDBToVersion(
    flyway: Flyway,
    schemaVersion: Int,
    schemaName: String
) {
    flyway.clean()
    if (schemaVersion != 0) {
        flyway.migrate()
    } else {
        flyway.baseline()
    }
    Database.connect(
        url = "$URL/$schemaName",
        user = USER,
        password = PASS
    )
}