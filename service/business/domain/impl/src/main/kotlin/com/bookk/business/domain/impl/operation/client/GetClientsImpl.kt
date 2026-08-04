package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class GetClientsImpl(
    private val clientsDataSource: ClientDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
): GetClients {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid): Result<List<ClientRemote>> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.READ)
            clientsDataSource.getClients(businessId).map { it.toRemote() }
        }
}
