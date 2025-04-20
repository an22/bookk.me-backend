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
    createDatabase()
}