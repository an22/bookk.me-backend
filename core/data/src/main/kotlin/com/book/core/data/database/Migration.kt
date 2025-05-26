package com.book.core.data.database

import com.bookk.core.AppLevelConstants
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.transactions.TransactionManager
import javax.sql.DataSource

fun createDatabase(
    schemaName: String = AppLevelConstants.dbSchemaName,
    driver: String = AppLevelConstants.dbDriver,
    dbUrl: String = AppLevelConstants.dbUrl,
    dbPort: String = AppLevelConstants.dbPort,
    dbUsername: String = AppLevelConstants.dbUsername,
    dbPassword: String = AppLevelConstants.dbPassword
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
        .executeInTransaction(true)
        .locations(Location("db/migration/$schemaName"))
        .load()
        .migrate()

    TransactionManager.defaultDatabase = Database.connect(
        datasource = dataSource,
        databaseConfig = DatabaseConfig {
            defaultSchema = Schema(schemaName)
        }
    )
}