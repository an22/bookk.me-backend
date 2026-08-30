package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.client.entity.Client
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface ClientDataSource {
    suspend fun createDetachedClient(businessId: Uuid, client: Client.Detached): Client
    suspend fun createIntegratedClient(businessId: Uuid, client: Client.Integrated): Client
    suspend fun getClients(businessId: Uuid): List<Client>
    suspend fun getClient(businessId: Uuid, phone: String): Client?
    suspend fun deleteClient(businessId: Uuid, id: Uuid): Boolean
    suspend fun updateIntegratedClients(
        userId: Uuid,
        name: String,
        lastName: String,
        email: String,
        phone: String?,
        updatedAt: Instant
    ): Int
    suspend fun anonymizeClientsByUserId(userId: Uuid): Int
}