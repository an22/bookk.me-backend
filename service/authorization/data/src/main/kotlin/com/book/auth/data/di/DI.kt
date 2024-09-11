package com.book.auth.data.di

import com.book.auth.data.repository.UserAuthLocalDataSourceImpl
import com.book.auth.data.repository.UserAuthRemoteDataSourceImpl
import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.datasource.UserAuthRemoteDataSource
import com.book.core.data.database.createDataSourceAndMigrateDb
import com.bookk.server.user.client.di.userClientModule
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.mysql.MySqlDialect

val authQualifier = named("authorization")

fun authDataModule() = module {
    includes(userClientModule())
    single<UserAuthLocalDataSource> { UserAuthLocalDataSourceImpl(get(authQualifier), get(authQualifier)) }
    single<UserAuthRemoteDataSource> { UserAuthRemoteDataSourceImpl() }
    single<Database>(authQualifier) {
        val dataSource= createDataSourceAndMigrateDb(authQualifier.value)
        Database.connect(
            dataSource = dataSource,
            logger = ConsoleLogger(threshold = LogLevel.INFO),
            dialect = MySqlDialect()
        )
    }
}