package com.book.auth.data.orm.entity

import com.book.auth.data.orm.table.AuthenticationTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class AuthenticationEntity(id: EntityID<Long>) : LongEntity(id) {
    var userId by AuthenticationTable.userId
    var email by AuthenticationTable.email

    companion object : LongEntityClass<AuthenticationEntity>(AuthenticationTable)
}