package com.bookk.notifications.domain.impl.notification

internal class NotificationParameters(
    val type: NotificationType,
    val push: PushNotification,
    val email: EmailNotification,
    val text: TextNotification
)

internal class PushNotification(
    val title: String,
    val subtitle: String,
)

internal class EmailNotification(
    val subject: String,
    val body: String,
)

internal class TextNotification(
    val text: String
)

internal enum class NotificationType {
    APPOINTMENT
}