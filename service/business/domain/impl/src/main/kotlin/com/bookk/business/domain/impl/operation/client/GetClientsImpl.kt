package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class GetClientsImpl(
    private val clientsDataSource: ClientDataSource,
    private val transactionManager: TransactionManager
): GetClients {
    override suspend fun invoke(businessId: Uuid): Result<List<ClientRemote>> = transactionManager.transaction {
        clientsDataSource.getClients(businessId).map { it.toRemote() }
    }
}