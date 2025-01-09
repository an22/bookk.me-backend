package com.book.user.data.orm.entity

import com.book.user.data.orm.table.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Long>) : LongEntity(id) {

    var name by UserTable.name
    var lastName by UserTable.lastName
    var email by UserTable.email
    var phone by UserTable.phone

    companion object : LongEntityClass<UserEntity>(UserTable)
}