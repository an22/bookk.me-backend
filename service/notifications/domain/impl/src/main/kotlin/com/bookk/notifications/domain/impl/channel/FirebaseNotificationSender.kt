package com.bookk.notifications.domain.impl.channel

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.notifications.domain.datasource.DeviceDataSource
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

internal class FirebaseNotificationSender(
    private val transactionManager: TransactionManager,
    private val deviceDataSource: DeviceDataSource,
    private val firebaseMessaging: FirebaseMessaging,
) : NotificationSender {

    private val logger = LoggerFactory.getLogger(FirebaseNotificationSender::class.java)

    override suspend fun send(toUserId: Uuid, params: NotificationParameters) = transactionManager.transaction {
        val devices = deviceDataSource.getByUserId(toUserId)
        val messages = devices
            .mapNotNull { it.notificationToken }
            .associateWith { token ->
                Message.builder()
                    .setFid(token)
                    .setNotification(
                        Notification.builder()
                            .setTitle(params.push.title)
                            .setBody(params.push.subtitle)
                            .build()
                    )
                    .build()
            }

        messages.forEach { (token, message) ->
            runCatching {
                firebaseMessaging.send(message)
            }.onFailure { error ->
                val deviceUuid = devices.first { it.notificationToken == token }.deviceUuid
                when {
                    error is FirebaseMessagingException && error.messagingErrorCode == UNREGISTERED -> {
                        deviceDataSource.updateToken(deviceUuid, null)
                    }
                    else -> logger.error("Failed to send message to device: $deviceUuid", error)
                }
            }
        }
    }
}