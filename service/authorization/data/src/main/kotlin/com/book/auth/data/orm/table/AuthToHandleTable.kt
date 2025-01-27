package com.book.auth.data.orm.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

internal object AuthToHandleTable : LongIdTable("auth_to_handle") {
    val authId = reference("auth_id", AuthenticationTable, onDelete = ReferenceOption.CASCADE)
    val userHandle = binary("handle", 64)
}