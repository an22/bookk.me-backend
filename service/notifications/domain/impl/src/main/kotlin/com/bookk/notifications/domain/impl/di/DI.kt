package com.bookk.notifications.domain.impl.di

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.notifications.domain.api.CreateDeviceEntry
import com.bookk.notifications.domain.api.DeleteDeviceByUUID
import com.bookk.notifications.domain.api.GetNotificationSettings
import com.bookk.notifications.domain.api.UpdateNotificationSettings
import com.bookk.notifications.domain.api.UpdatePushNotificationToken
import com.bookk.notifications.domain.impl.CreateDeviceEntryImpl
import com.bookk.notifications.domain.impl.DeleteDeviceByUUIDImpl
import com.bookk.notifications.domain.impl.GetNotificationSettingsImpl
import com.bookk.notifications.domain.impl.UpdateNotificationSettingsImpl
import com.bookk.notifications.domain.impl.UpdatePushNotificationTokenImpl
import com.bookk.notifications.domain.impl.event.NotificationEventHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun notificationsDomainModule() = module {
    factoryOf(::NotificationEventHandler) bind EventHandler::class
    factoryOf(::CreateDeviceEntryImpl) bind CreateDeviceEntry::class
    factoryOf(::DeleteDeviceByUUIDImpl) bind DeleteDeviceByUUID::class
    factoryOf(::UpdatePushNotificationTokenImpl) bind UpdatePushNotificationToken::class
    factoryOf(::UpdateNotificationSettingsImpl) bind UpdateNotificationSettings::class
    factoryOf(::GetNotificationSettingsImpl) bind GetNotificationSettings::class
}
