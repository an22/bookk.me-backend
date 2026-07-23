package com.bookk.notifications.data.di

import com.bookk.core.data.database.createDatabase
import com.bookk.notifications.data.datasource.DeviceDataSourceImpl
import com.bookk.notifications.data.datasource.NotificationSettingsDataSourceImpl
import com.bookk.notifications.data.datasource.NotificationTargetDataSourceImpl
import com.bookk.notifications.domain.datasource.DeviceDataSource
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import com.bookk.notifications.domain.datasource.NotificationTargetDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun notificationsDataModule() = module {
    createDatabase()
    singleOf(::DeviceDataSourceImpl) bind DeviceDataSource::class
    singleOf(::NotificationSettingsDataSourceImpl) bind NotificationSettingsDataSource::class
    singleOf(::NotificationTargetDataSourceImpl) bind NotificationTargetDataSource::class
}
