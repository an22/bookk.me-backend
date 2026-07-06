package com.bookk.notifications.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable

object NotificationEmailTargetTable : BaseUUIDTable("notification_email_targets") {
    val userId = uuid("user_id")
    val email = text("email")

    init {
        uniqueIndex(userId)
    }
}
