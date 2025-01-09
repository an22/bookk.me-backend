package com.book.core.data.database

import MigrationUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import javax.sql.DataSource

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun createDatabase(
    version: Int,
    schemaName: String,
    driver: String,
    dbUrl: String,
    dbPort: String,
    dbUsername: String,
    dbPassword: String,
    tables: Array<Table>
) {
    val dataSource: DataSource = HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = "$dbUrl:$dbPort?allowPublicKeyRetrieval=true"
            username = dbUsername
            password = dbPassword
            schema = schemaName
            validate()
        }
    )
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .baselineOnMigrate(true)
        .defaultSchema(schemaName)
        .locations("filesystem:db/migration/$schemaName")
        .load()

    val schema = Schema(schemaName)
    Database.connect(datasource = dataSource)
    transaction {
        SchemaUtils.createSchema(schema)
    }
    Database.connect(
        datasource = dataSource,
        databaseConfig = DatabaseConfig {
            defaultSchema = schema
        }
    )

    transaction {
        val dir = File("db/migration/$schemaName")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        MigrationUtils.generateMigrationScript(
            tables = tables,
            scriptDirectory = "db/migration/$schemaName",
            scriptName = "V${version}__migration_script"
        )
    }

    transaction {
        flyway.migrate()
    }
}