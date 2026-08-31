package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.RejectEmployeeInvitation
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import com.bookk.server.user.client.UserClient
import kotlin.uuid.Uuid

internal class RejectEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val userClient: UserClient,
    private val transactionManager: TransactionManager
) : RejectEmployeeInvitation {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, id: Uuid): Result<Unit> =
        transactionManager.transaction {
            val invitation = invitationDataSource.getInvitation(businessId, id) ?: throw Error.NotFound()
            val requestUser = userClient.getUserById(requestUserId).getOrThrow()
            if (invitation.email != requestUser.email) throw Error.OperationNotAllowed()
            if (invitation.status != EmployeeInvitationStatus.PENDING) {
                throw RejectEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
            if (!invitationDataSource.rejectInvitation(id)) {
                throw RejectEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
        }
}
