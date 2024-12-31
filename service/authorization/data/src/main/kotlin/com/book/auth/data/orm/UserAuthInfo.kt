package com.book.auth.data.orm

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object UserAuthInfo : Table<Nothing>("user_auth_info") {
    val id = long("id").primaryKey()
    val userId = long("user_id")
    val login = varchar("login")
    val passwordHash = varchar("password_hash")
    val totpSecret = varchar("totp_secret")
    val role = int("role")
}

object UserAuthInfoV2 : Table<Nothing>("user_auth_info_v2") {
    val id = long("id").primaryKey()
    val login = varchar("login")
    val role = int("role")
}