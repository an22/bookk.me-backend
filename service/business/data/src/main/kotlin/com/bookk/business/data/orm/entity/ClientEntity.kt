package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.ClientTable
import com.bookk.business.domain.api.entity.Client
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.uuid.toKotlinUuid

internal class ClientEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    val businessId by ClientTable.businessId
    val name by ClientTable.name
    val lastName by ClientTable.lastName
    val phone by ClientTable.phone
    val userId by ClientTable.userId

    companion object : DecoratorUUIDEntityClass<ClientEntity>(ClientTable)

    fun toDomain(): Client {
        return when (val userId = userId) {
            null -> Client.Detached(
                id = id.value.toKotlinUuid(),
                name = name,
                lastName = lastName,
                phone = phone
            )

            else -> Client.Integrated(
                id = id.value.toKotlinUuid(),
                name = name,
                lastName = lastName,
                phone = phone,
                userId = userId.toKotlinUuid()
            )
        }
    }
}