package com.bookk.notifications.data.di

import com.bookk.core.data.database.createDatabase
import com.bookk.notifications.data.datasource.DeviceDataSourceImpl
import com.bookk.notifications.domain.datasource.DeviceDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun notificationsDataModule() = module {
    createDatabase()
    singleOf(::DeviceDataSourceImpl) bind DeviceDataSource::class
}
