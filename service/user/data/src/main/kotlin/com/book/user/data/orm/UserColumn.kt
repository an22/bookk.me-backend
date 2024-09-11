package com.book.user.data.orm

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object UserColumn : Table<Nothing>("user_profile") {
    val id = long("id").primaryKey()
    val name = varchar("name")
    val lastName = varchar("last_name")
    val email = varchar("email")
    val phone = varchar("phone")
    val role = int("role")
}