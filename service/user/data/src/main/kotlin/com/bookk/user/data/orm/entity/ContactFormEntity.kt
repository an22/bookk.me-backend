package com.bookk.user.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.user.data.orm.table.ContactFormTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class ContactFormEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var user by UserEntity referencedOn ContactFormTable.userId
    var usageLogs by ContactFormTable.usageLogs
    var text by ContactFormTable.text
    var createdAt by ContactFormTable.createdAt
    var updatedAt by ContactFormTable.updatedAt
    var status by ContactFormTable.status

    companion object : DecoratorUUIDEntityClass<ContactFormEntity>(ContactFormTable)
}