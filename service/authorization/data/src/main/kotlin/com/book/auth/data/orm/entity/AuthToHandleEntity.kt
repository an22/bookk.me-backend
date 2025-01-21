package com.book.auth.data.orm.entity

import com.book.auth.data.orm.table.AuthToHandleTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class AuthToHandleEntity(id: EntityID<Long>) : LongEntity(id) {
    var authentication by AuthenticationEntity referencedOn AuthToHandleTable.authId
    var userHandle by AuthToHandleTable.userHandle

    companion object : LongEntityClass<AuthToHandleEntity>(AuthToHandleTable)
}