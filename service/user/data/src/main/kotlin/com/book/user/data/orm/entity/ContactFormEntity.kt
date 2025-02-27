package com.book.user.data.orm.entity

import com.book.user.data.orm.table.ContactFormTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class ContactFormEntity(id: EntityID<Long>) : LongEntity(id) {

    var user by UserEntity referencedOn ContactFormTable.userId
    var usageLogs by ContactFormTable.usageLogs
    var text by ContactFormTable.text
    var createdAt by ContactFormTable.createdAt
    var updatedAt by ContactFormTable.updatedAt

    companion object : LongEntityClass<ContactFormEntity>(ContactFormTable)
}