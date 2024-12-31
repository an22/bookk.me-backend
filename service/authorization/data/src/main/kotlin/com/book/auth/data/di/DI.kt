package com.book.auth.data.di

import com.book.auth.data.repository.CredentialRepositoryImpl
import com.book.auth.data.repository.PassKeyDataSourceImpl
import com.book.auth.data.repository.UserAuthDataSourceImpl
import com.book.auth.domain.api.datasource.PassKeyDataSource
import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.core.data.database.createDataSourceAndMigrateDb
import com.bookk.server.user.client.di.userClientModule
import com.yubico.webauthn.CredentialRepository
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.mysql.MySqlDialect

fun authDataModule() = module {
    includes(userClientModule(System.getenv("BOOKK_ME_SERVICE_NAME")))
    single<UserAuthDataSource> { UserAuthDataSourceImpl(get(), get()) }
    single<PassKeyDataSource> { PassKeyDataSourceImpl(get()) }
    single<CredentialRepository> { CredentialRepositoryImpl(get()) }
    single<Database> {
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