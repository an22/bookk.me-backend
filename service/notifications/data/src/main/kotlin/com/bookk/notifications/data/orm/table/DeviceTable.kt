package com.bookk.notifications.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable

object DeviceTable : BaseUUIDTable("device") {
    val authId = uuid("auth_id")
    val deviceUuid = uuid("device_uuid").uniqueIndex()
    val userId = uuid("user_id")
    val notificationToken = text("notification_token").nullable()

    init {
        index(isUnique = true, authId, deviceUuid, userId)
    }
}
