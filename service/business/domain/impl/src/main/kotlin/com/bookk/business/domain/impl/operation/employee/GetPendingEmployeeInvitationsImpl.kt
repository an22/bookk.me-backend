package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitations
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.user.client.UserClient
import kotlin.uuid.Uuid

internal class GetPendingEmployeeInvitationsImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val userClient: UserClient,
    private val transactionManager: TransactionManager
) : GetPendingEmployeeInvitations {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<List<EmployeeInvitation>> =
        transactionManager.transaction {
            val requestUser = userClient.getUserById(userId).getOrThrow()
            invitationDataSource.getPendingInvitations(businessId, requestUser.email)
        }
}
