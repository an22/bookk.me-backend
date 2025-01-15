package com.book.core.data.database

import MigrationUtils
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

private const val URL = "jdbc:mariadb://127.0.0.1:3308"
private const val USER = "root"
private const val PASS = "migration"
private const val DRIVER = "org.mariadb.jdbc.Driver"

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun createMigrationScriptFor(
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
    transaction {
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

private fun moveDBWithVersion(
    schemaVersion: Int,
    migrationsFolderPath: String,
    schemaName: String
) {
    Database.connect(
        url = URL,
        user = USER,
        driver = DRIVER,
        password = PASS
    )
    transaction {
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
        transaction {
            SchemaUtils.createSchema(Schema(schemaName))
            SchemaUtils.setSchema(Schema(schemaName))
        }
    }
}