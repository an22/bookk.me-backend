package com.book.user.data.orm.table

import org.jetbrains.exposed.dao.id.LongIdTable

object UserTable : LongIdTable("profile") {
    val name = varchar("name", 255)
    val lastName = varchar("last_name", 255)
    val email = varchar("email", 320)
    val phone = varchar("phone", 255).nullable()
}