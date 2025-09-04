package com.book.core.data.database

import com.bookk.core.AppLevelConstants
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.jetbrains.exposed.v1.dao.EntityLifecycleInterceptor
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
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
        url = "r2dbc:$dbUrl:$dbPort/$schemaName",
        driver = "mariadb",
        user = dbUsername,
        password = dbPassword
    )
    R2dbcTransaction.globalInterceptors.removeIf { it is EntityLifecycleInterceptor }
}