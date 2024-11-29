package com.book.user.data.di

import com.book.core.data.database.createDataSourceAndMigrateDb
import com.book.user.data.repository.UserAuthLocalDataSourceImpl
import com.book.user.domain.api.datasource.UserLocalDataSource
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.mysql.MySqlDialect

fun userDataModule(qualifier: Qualifier) = module {
    single<UserLocalDataSource> { UserAuthLocalDataSourceImpl(get(qualifier), get(qualifier)) }
    single<Database>(qualifier) {
        val dataSource= createDataSourceAndMigrateDb(
            schemaName = System.getenv("BOOKK_ME_DB_SCHEME"),
            driver = System.getenv("BOOKK_ME_DB_DRIVER"),
            dbUrl = System.getenv("BOOKK_ME_DB_URL"),
            dbPort = System.getenv("BOOKK_ME_DB_PORT"),
            dbUsername = System.getenv("BOOKK_ME_DB_USER"),
            dbPassword = System.getenv("BOOKK_ME_DB_PASSWORD")
        )
        Database.connect(
            dataSource = dataSource,
            logger = ConsoleLogger(threshold = LogLevel.INFO),
            dialect = MySqlDialect()
        )
    }
}