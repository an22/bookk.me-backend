package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.operation.DeleteClient
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class DeleteClientImpl(
    private val transactionManager: TransactionManager,
    private val clientDataSource: ClientDataSource
) : DeleteClient {
    override suspend fun invoke(businessId: Uuid, id: Uuid): Result<Unit> = transactionManager.transaction {
        if (!clientDataSource.deleteClient(businessId, id)) {
            throw DeleteClient.Error.NotFound
        }
    }
}