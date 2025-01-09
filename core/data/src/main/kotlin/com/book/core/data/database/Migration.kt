package com.book.core.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.Schema
import javax.sql.DataSource

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun createDatabase(
    schemaName: String,
    driver: String,
    dbUrl: String,
    dbPort: String,
    dbUsername: String,
    dbPassword: String
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
    Flyway.configure()
        .dataSource(dataSource)
        .baselineOnMigrate(true)
        .createSchemas(true)
        .defaultSchema(schemaName)
        .locations(Location("db/migration/$schemaName"))
        .load()
        .migrate()

    Database.connect(
        datasource = dataSource,
        databaseConfig = DatabaseConfig {
            defaultSchema = Schema(schemaName)
        }
    )
}