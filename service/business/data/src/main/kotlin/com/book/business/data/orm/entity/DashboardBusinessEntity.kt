package com.book.business.data.orm.entity

import com.book.business.data.orm.table.BusinessDashboardTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class DashboardBusinessEntity(id: EntityID<Long>) : LongEntity(id) {
    val userId by BusinessDashboardTable.businessId
    val business by BusinessEntity referencedOn BusinessDashboardTable.businessId
    val createdAt by BusinessDashboardTable.createdAt
    val updatedAt by BusinessDashboardTable.updatedAt

    companion object : LongEntityClass<DashboardBusinessEntity>(BusinessDashboardTable)
}