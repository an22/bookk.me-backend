package com.book.core.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import javax.sql.DataSource

fun createDataSourceAndMigrateDb(
    schemaName: String,
    driver: String,
    dbUrl: String,
    dbPort: String,
    dbUsername: String,
    dbPassword: String,
): DataSource {
    val migrationDataSource: DataSource = HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = "$dbUrl:$dbPort?allowPublicKeyRetrieval=true"
            username = dbUsername
            password = dbPassword
            validate()
        }
    )

    Flyway.configure()
        .dataSource(migrationDataSource)
        .createSchemas(true)
        .defaultSchema(schemaName)
        .locations(Location("db/migration/$schemaName"))
        .load()
        .migrate()

    return HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = "$dbUrl:$dbPort/$schemaName?allowPublicKeyRetrieval=true"
            username = dbUsername
            password = dbPassword
            validate()
        }
    )
}