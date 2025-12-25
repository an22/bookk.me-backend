package com.bookk.auth.data.orm.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

internal object AuthDeviceTable : UUIDTable("auth_device") {
    val userAuthId = reference("user_auth_id", AuthenticationTable, onDelete = ReferenceOption.CASCADE).index()
    val deviceUUID = uuid("device_uuid")
    val deviceName = varchar("device_name", 255)
    val refreshTokenId = uuid("refresh_token_id").nullable()
    val isSignedIn = bool("is_signed_in")
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }

    init {
        uniqueIndex(userAuthId, deviceUUID)
    }
}