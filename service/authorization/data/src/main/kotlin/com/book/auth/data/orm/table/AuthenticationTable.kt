package com.book.auth.data.orm.table

import org.jetbrains.exposed.dao.id.LongIdTable

internal object AuthenticationTable : LongIdTable("authentication") {
    val userId = long("user_id").uniqueIndex()
    val email = varchar("email", 320)
}