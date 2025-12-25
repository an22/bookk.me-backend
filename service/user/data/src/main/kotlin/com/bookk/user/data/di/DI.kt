package com.bookk.user.data.di

import com.bookk.core.data.database.createDatabase
import com.bookk.user.data.datasource.CommunicationDataSourceImpl
import com.bookk.user.data.datasource.UserDataSourceImpl
import com.bookk.user.domain.datasource.CommunicationDataSource
import com.bookk.user.domain.datasource.UserDataSource
import org.koin.dsl.module

fun userDataModule() = module {
    single<UserDataSource> { UserDataSourceImpl(get()) }
    single<CommunicationDataSource> { CommunicationDataSourceImpl() }
    createDatabase()
}