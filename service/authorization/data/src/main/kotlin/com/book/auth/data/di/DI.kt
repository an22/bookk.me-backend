package com.book.auth.data.di

import com.book.auth.data.repository.UserAuthLocalDataSourceImpl
import com.book.auth.data.repository.UserAuthRemoteDataSourceImpl
import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.datasource.UserAuthRemoteDataSource
import com.book.core.data.database.createDataSourceAndMigrateDb
import com.bookk.server.user.client.di.userClientModule
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.mysql.MySqlDialect

fun authDataModule(qualifier: Qualifier) = module {
    includes(userClientModule(System.getenv("BOOKK_ME_SERVICE_NAME")))
    single<UserAuthLocalDataSource> { UserAuthLocalDataSourceImpl(get(qualifier), get(qualifier)) }
    single<UserAuthRemoteDataSource> { UserAuthRemoteDataSourceImpl() }
    single<Database>(qualifier) {
        val dataSource = createDataSourceAndMigrateDb(
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