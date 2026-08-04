package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class CreateClientImpl(
    private val transactionManager: TransactionManager,
    private val clientDataSource: ClientDataSource,
    private val businessDataSource: BusinessDataSource
) : CreateClient {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, client: Client): Result<ClientRemote> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.EDIT)
            if (client.name.length > MAX_NAME_LENGTH) throw CreateClient.Error.ClientValidationError()
            if (client.lastName.length > MAX_NAME_LENGTH) throw CreateClient.Error.ClientValidationError()
            if (clientDataSource.getClient(businessId, client.phone) != null) throw CreateClient.Error.ClientExist()

            when (client) {
                is Client.Detached -> clientDataSource.createDetachedClient(businessId, client)
                is Client.Integrated -> clientDataSource.createIntegratedClient(businessId, client)
            }.toRemote()
        }

    companion object {
        const val MAX_NAME_LENGTH = 512
    }
}
