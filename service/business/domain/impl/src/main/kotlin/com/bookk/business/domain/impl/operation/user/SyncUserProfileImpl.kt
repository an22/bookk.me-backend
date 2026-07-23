package com.bookk.business.domain.impl.operation.user

import com.bookk.business.domain.api.user.operation.SyncUserProfile
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class SyncUserProfileImpl(
    private val transactionManager: TransactionManager,
    private val clientDataSource: ClientDataSource
) : SyncUserProfile {
    override suspend fun invoke(
        userId: Uuid,
        name: String,
        lastName: String,
        phone: String,
        email: String
    ): Result<Unit> = transactionManager.transaction {
        clientDataSource.updateIntegratedClients(userId, name, lastName, phone, email)
        Unit
    }
}
