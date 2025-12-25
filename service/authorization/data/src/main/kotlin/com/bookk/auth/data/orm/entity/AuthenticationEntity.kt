package com.bookk.auth.data.orm.entity

import com.bookk.auth.data.orm.table.AuthenticationTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class AuthenticationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var userId by AuthenticationTable.userId
    var uuid by AuthenticationTable.uuid
    var createdAt by AuthenticationTable.createdAt
    var updatedAt by AuthenticationTable.updatedAt

    companion object : DecoratorUUIDEntityClass<AuthenticationEntity>(AuthenticationTable)
}