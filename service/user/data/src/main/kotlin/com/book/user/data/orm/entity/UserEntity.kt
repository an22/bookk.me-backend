package com.book.user.data.orm.entity

import com.book.core.data.R2dbcUUIDEntityClass
import com.book.user.data.orm.table.UserTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var name by UserTable.name
    var lastName by UserTable.lastName
    var email by UserTable.email
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt

    companion object : R2dbcUUIDEntityClass<UserEntity>(UserTable)
}