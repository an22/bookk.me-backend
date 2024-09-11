package com.book.user.data.di

import com.book.core.data.database.createDataSourceAndMigrateDb
import com.book.user.data.repository.UserAuthLocalDataSourceImpl
import com.book.user.domain.api.datasource.UserLocalDataSource
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.mysql.MySqlDialect

val userQualifier = named("user")

fun userDataModule() = module {
    single<UserLocalDataSource> { UserAuthLocalDataSourceImpl(get(userQualifier), get(userQualifier)) }
    single<Database>(userQualifier) {
        val dataSource = createDataSourceAndMigrateDb(userQualifier.value)
        Database.connect(
            dataSource = dataSource,
            logger = ConsoleLogger(threshold = LogLevel.INFO),
            dialect = MySqlDialect()
        )
    }
}