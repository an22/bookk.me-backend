package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.entity.Client
import kotlin.uuid.Uuid

interface ClientDataSource {
    suspend fun createDetachedClient(businessId: Uuid, client: Client.Detached): Client
    suspend fun createIntegratedClient(businessId: Uuid, client: Client.Integrated): Client
    suspend fun getClients(businessId: Uuid): List<Client>
    suspend fun getClient(businessId: Uuid, phone: String): Client?
    suspend fun deleteClient(businessId: Uuid, id: Uuid): Boolean
}