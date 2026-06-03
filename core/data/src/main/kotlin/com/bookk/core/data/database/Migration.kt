package com.bookk.core.data.database

import com.bookk.core.AppLevelConstants
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

fun createDatabase(
    schemaName: String = AppLevelConstants.dbSchemaName,
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

    TransactionManager.defaultDatabase = Database.connect(
        url = "jdbc:$dbUrl:$dbPort/$schemaName?rewriteBatchedStatements=true",
        user = dbUsername,
        password = dbPassword
    )
}