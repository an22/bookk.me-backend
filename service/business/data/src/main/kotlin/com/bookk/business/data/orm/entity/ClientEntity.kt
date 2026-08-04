package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.ClientTable
import com.bookk.business.domain.api.client.entity.Client
import com.bookk.core.data.DecoratorUuidEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import kotlin.uuid.Uuid

internal class ClientEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    val businessId by ClientTable.businessId
    val name by ClientTable.name
    val lastName by ClientTable.lastName
    val phone by ClientTable.phone
    val email by ClientTable.email
    val userId by ClientTable.userId

    companion object : DecoratorUuidEntityClass<ClientEntity>(ClientTable)

    fun toDomain(): Client {
        return when (val userId = userId) {
            null -> Client.Detached(
                id = id.value,
                name = name,
                lastName = lastName,
                phone = phone,
                email = email
            )

            else -> Client.Integrated(
                id = id.value,
                name = name,
                lastName = lastName,
                phone = phone,
                email = email,
                userId = userId
            )
        }
    }
}