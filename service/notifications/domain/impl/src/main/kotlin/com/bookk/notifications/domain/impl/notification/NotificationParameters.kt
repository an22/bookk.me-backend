package com.bookk.notifications.domain.impl.notification

class NotificationParameters(
    val push: PushNotification,
    val email: EmailNotification,
    val text: TextNotification
)

class PushNotification(
    val title: String,
    val subtitle: String,
)

class EmailNotification(
    val subject: String,
    val body: String,
)

class TextNotification(
    val text: String
)