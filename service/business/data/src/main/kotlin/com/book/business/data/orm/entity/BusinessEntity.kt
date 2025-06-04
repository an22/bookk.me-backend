package com.book.business.data.orm.entity

import com.book.business.data.orm.table.BusinessTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class BusinessEntity(id: EntityID<Long>) : LongEntity(id) {
    var userId by BusinessTable.userId
    var name by BusinessTable.name
    var description by BusinessTable.description
    var latitude by BusinessTable.latitude
    var longitude by BusinessTable.longitude
    var currency by BusinessTable.currency
    var instagram by BusinessTable.instagram
    var telegram by BusinessTable.telegram
    var viber by BusinessTable.viber
    var whatsapp by BusinessTable.whatsapp
    var phone by BusinessTable.phone
    var createdAt by BusinessTable.createdAt
    var updatedAt by BusinessTable.updatedAt

    companion object : LongEntityClass<BusinessEntity>(BusinessTable)
}