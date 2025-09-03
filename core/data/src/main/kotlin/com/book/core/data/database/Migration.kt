package com.book.core.data.database

import com.bookk.core.AppLevelConstants
import io.r2dbc.spi.ConnectionFactoryOptions
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.jetbrains.exposed.v1.core.Schema
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager

fun createDatabase(
    schemaName: String = AppLevelConstants.dbSchemaName,
    driver: String = AppLevelConstants.dbDriver,
    dbUrl: String = AppLevelConstants.dbUrl,
    dbPort: String = AppLevelConstants.dbPort,
    dbUsername: String = AppLevelConstants.dbUsername,
    dbPassword: String = AppLevelConstants.dbPassword
) {
    Flyway.configure()
        .dataSource(
            "jdbc:$dbUrl:$dbPort",
            dbUsername,
            dbPassword
        )
        .baselineOnMigrate(true)
        .createSchemas(true)
        .defaultSchema(schemaName)
        .executeInTransaction(true)
        .locations(Location("db/migration/$schemaName"))
        .load()
        .migrate()

    TransactionManager.defaultDatabase = R2dbcDatabase.connect(
        url = "r2dbc:$dbUrl:$dbPort",
        databaseConfig = {
            defaultSchema = Schema(schemaName)
            connectionFactoryOptions {
                option(ConnectionFactoryOptions.USER, dbUsername)
                option(ConnectionFactoryOptions.PASSWORD, dbPassword)
            }
        }
    )
}