package com.book.user.data.di

import com.book.core.data.database.createDatabase
import com.book.user.data.datasource.CommunicationDataSourceImpl
import com.book.user.data.datasource.UserDataSourceImpl
import com.book.user.domain.datasource.CommunicationDataSource
import com.book.user.domain.datasource.UserDataSource
import org.koin.dsl.module

fun userDataModule() = module {
    single<UserDataSource> { UserDataSourceImpl(get()) }
    single<CommunicationDataSource> { CommunicationDataSourceImpl() }
    createDatabase(
        schemaName = System.getenv("BOOKK_ME_DB_SCHEME"),
        driver = System.getenv("BOOKK_ME_DB_DRIVER"),
        dbUrl = System.getenv("BOOKK_ME_DB_URL"),
        dbPort = System.getenv("BOOKK_ME_DB_PORT"),
        dbUsername = System.getenv("BOOKK_ME_DB_USER"),
        dbPassword = System.getenv("BOOKK_ME_DB_PASSWORD")
    )
}