package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.ServiceGroupTable
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.uuid.toKotlinUuid

internal class ServiceGroupEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    val businessId by ServiceGroupTable.businessId
    val name by ServiceGroupTable.name
    val createdAt by ServiceGroupTable.createdAt
    val updatedAt by ServiceGroupTable.updatedAt

    companion object : DecoratorUUIDEntityClass<ServiceGroupEntity>(ServiceGroupTable)

    fun toDomain(): ServiceGroup {
        return ServiceGroup(
            id = id.value.toKotlinUuid(),
            businessId = businessId.value.toKotlinUuid(),
            name = name,
            createdAt = createdAt
        )
    }
}