package com.book.user.data.di

import com.book.core.data.database.createDatabase
import com.book.user.data.orm.table.UserTable
import com.book.user.data.repository.UserDataSourceImpl
import com.book.user.domain.api.datasource.UserDataSource
import org.jetbrains.exposed.sql.Table
import org.koin.dsl.module

fun userDataModule() = module {
    single<UserDataSource> { UserDataSourceImpl(get()) }

    createDatabase(
        schemaName = System.getenv("BOOKK_ME_DB_SCHEME"),
        driver = System.getenv("BOOKK_ME_DB_DRIVER"),
        dbUrl = System.getenv("BOOKK_ME_DB_URL"),
        dbPort = System.getenv("BOOKK_ME_DB_PORT"),
        dbUsername = System.getenv("BOOKK_ME_DB_USER"),
        dbPassword = System.getenv("BOOKK_ME_DB_PASSWORD")
    )
}

internal fun userTables(): Array<Table> {
    return arrayOf(
        UserTable
    )
}