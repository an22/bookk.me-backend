package com.book.auth.data.orm

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object AuthDevice : Table<Nothing>("auth_device") {
    val id = long("id").primaryKey()
    val userAuthId = long("user_auth_id")
    val deviceName = varchar("device_name")
    val refreshToken = varchar("refresh_token")
    val isSignedIn = boolean("is_signed_in")
}