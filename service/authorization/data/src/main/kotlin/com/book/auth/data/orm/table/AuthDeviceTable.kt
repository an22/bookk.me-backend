package com.book.auth.data.orm.table

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

internal object AuthDeviceTable : LongIdTable("auth_device") {
    val userAuthId = reference("user_auth_id", AuthenticationTable, onDelete = ReferenceOption.CASCADE).index()
    val deviceUUID = varchar("device_uuid", 128).uniqueIndex()
    val deviceName = varchar("device_name", 255)
    val refreshToken = varchar("refresh_token", 255).nullable()
    val isSignedIn = bool("is_signed_in")
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at")
}