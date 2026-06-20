package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class BusinessEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var userId by BusinessTable.userId
    var name by BusinessTable.name
    var description by BusinessTable.description
    var address by BusinessTable.address
    var latitude by BusinessTable.latitude
    var longitude by BusinessTable.longitude
    var timezone by BusinessTable.timezone
    var currency by BusinessTable.currency
    var instagram by BusinessTable.instagram
    var telegram by BusinessTable.telegram
    var viber by BusinessTable.viber
    var whatsapp by BusinessTable.whatsapp
    var phone by BusinessTable.phone
    var createdAt by BusinessTable.createdAt
    var updatedAt by BusinessTable.updatedAt

    companion object : DecoratorUUIDEntityClass<BusinessEntity>(BusinessTable)
}