package com.bookk.notifications.domain.impl.di

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.notifications.domain.api.CreateDeviceEntry
import com.bookk.notifications.domain.api.DeleteDeviceByUUID
import com.bookk.notifications.domain.api.DeleteUserNotificationData
import com.bookk.notifications.domain.api.GetNotificationSettings
import com.bookk.notifications.domain.api.UpdateNotificationSettings
import com.bookk.notifications.domain.api.UpdatePushNotificationToken
import com.bookk.notifications.domain.api.entity.CommunicationChannel
import com.bookk.notifications.domain.impl.CreateDeviceEntryImpl
import com.bookk.notifications.domain.impl.DeleteDeviceByUUIDImpl
import com.bookk.notifications.domain.impl.DeleteUserNotificationDataImpl
import com.bookk.notifications.domain.impl.GetNotificationSettingsImpl
import com.bookk.notifications.domain.impl.UpdateDeviceLanguage
import com.bookk.notifications.domain.impl.UpdateNotificationSettingsImpl
import com.bookk.notifications.domain.impl.UpdatePushNotificationTokenImpl
import com.bookk.notifications.domain.impl.UpdateTargetInformation
import com.bookk.notifications.domain.impl.channel.EmailNotificationSender
import com.bookk.notifications.domain.impl.channel.FirebaseNotificationSender
import com.bookk.notifications.domain.impl.channel.NotificationSender
import com.bookk.notifications.domain.impl.channel.TelegramNotificationSender
import com.bookk.notifications.domain.impl.event.NotificationEventHandler
import com.bookk.notifications.domain.impl.notification.SendNotification
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun notificationsDomainModule() = module {
    factoryOf(::NotificationEventHandler) bind EventHandler::class
    factoryOf(::CreateDeviceEntryImpl) bind CreateDeviceEntry::class
    factoryOf(::DeleteDeviceByUUIDImpl) bind DeleteDeviceByUUID::class
    factoryOf(::DeleteUserNotificationDataImpl) bind DeleteUserNotificationData::class
    factoryOf(::UpdatePushNotificationTokenImpl) bind UpdatePushNotificationToken::class
    factoryOf(::UpdateNotificationSettingsImpl) bind UpdateNotificationSettings::class
    factoryOf(::GetNotificationSettingsImpl) bind GetNotificationSettings::class
    singleOf(::FirebaseNotificationSender) bind NotificationSender::class
    singleOf(::EmailNotificationSender) bind NotificationSender::class
    singleOf(::TelegramNotificationSender) bind NotificationSender::class
    singleOf(::SendNotification)
    singleOf(::UpdateTargetInformation)
    singleOf(::UpdateDeviceLanguage)
    single<Map<CommunicationChannel, NotificationSender>> {
        mapOf(
            CommunicationChannel.EMAIL to get<EmailNotificationSender>(),
            CommunicationChannel.TELEGRAM to get<TelegramNotificationSender>(),
            CommunicationChannel.PUSH_NOTIFICATIONS to get<FirebaseNotificationSender>()
        )
    }
    single { FirebaseMessaging.getInstance() }
}
