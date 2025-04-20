package com.book.auth.data.orm.table

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

internal object AuthDeviceTable : LongIdTable("auth_device") {
    val userAuthId = reference("user_auth_id", AuthenticationTable, onDelete = ReferenceOption.CASCADE).index()
    val deviceUUID = varchar("device_uuid", 36)
    val deviceName = varchar("device_name", 255)
    val refreshTokenId = varchar("refresh_token_id", 36).nullable()
    val isSignedIn = bool("is_signed_in")
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }

    init {
        uniqueIndex(userAuthId, deviceUUID)
    }
}