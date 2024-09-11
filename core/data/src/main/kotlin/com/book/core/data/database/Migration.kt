package com.book.core.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import javax.sql.DataSource

fun createDataSourceAndMigrateDb(schemaName: String): DataSource {
    val migrationDataSource: DataSource = HikariDataSource(
        HikariConfig().apply {
            driverClassName = System.getenv("me.bookk.db_driver")
            jdbcUrl = System.getenv("me.bookk.db_url")
            username = System.getenv("me.bookk.db_user")
            password = System.getenv("me.bookk.db_password")
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
            driverClassName = System.getenv("me.bookk.db_driver")
            jdbcUrl = System.getenv("me.bookk.db_url") + "/" + schemaName
            username = System.getenv("me.bookk.db_user")
            password = System.getenv("me.bookk.db_password")
            validate()
        }
    )
}