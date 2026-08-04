package com.bookk.business.data.datasource

import com.bookk.business.data.orm.entity.ClientEntity
import com.bookk.business.data.orm.table.ClientTable
import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class ClientDataSourceImpl : DataSource(), ClientDataSource {
    override suspend fun createDetachedClient(businessId: Uuid, client: Client.Detached): Client = dbQuery {
        val id = ClientTable.insertAndGetId {
            it[this.businessId] = businessId.toJavaUuid()
            it[name] = client.name.trim()
            it[lastName] = client.lastName.trim()
            it[phone] = client.phone.trim()
            it[email] = client.email.trim()
            it[userId] = null
        }
        client.copy(
            id = id.value.toKotlinUuid()
        )
    }

    override suspend fun createIntegratedClient(businessId: Uuid, client: Client.Integrated): Client = dbQuery {
        val id = ClientTable.insertAndGetId {
            it[this.businessId] = businessId.toJavaUuid()
            it[name] = client.name.trim()
            it[lastName] = client.lastName.trim()
            it[phone] = client.phone.trim()
            it[email] = client.email.trim()
            it[userId] = client.userId.toJavaUuid()
        }
        client.copy(
            id = id.value.toKotlinUuid()
        )
    }

    override suspend fun getClients(businessId: Uuid): List<Client> = dbQuery {
        ClientEntity.find {
            ClientTable.businessId eq businessId.toJavaUuid()
        }.map(ClientEntity::toDomain)
    }

    override suspend fun getClient(businessId: Uuid, phone: String): Client? = dbQuery {
        ClientEntity.find {
            (ClientTable.businessId eq businessId.toJavaUuid()) and (ClientTable.phone eq phone)
        }
            .map(ClientEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun deleteClient(businessId: Uuid, id: Uuid) = dbQuery {
        ClientTable.deleteWhere {
            (ClientTable.businessId eq businessId.toJavaUuid()) and (ClientTable.id eq id.toJavaUuid())
        } != 0
    }

    override suspend fun updateIntegratedClients(
        userId: Uuid,
        name: String,
        lastName: String,
        email: String,
        updatedAt: Instant
    ): Int = dbQuery {
        ClientTable.update(
            where = {
                (ClientTable.userId eq userId.toJavaUuid()) and
                    (ClientTable.sourceUpdatedAt.isNull() or (ClientTable.sourceUpdatedAt less updatedAt))
            }
        ) {
            it[this.name] = name.trim()
            it[this.lastName] = lastName.trim()
            it[this.email] = email.trim()
            it[sourceUpdatedAt] = updatedAt
        }
    }
}