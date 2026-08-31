package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.RevokeEmployeeInvitation
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class RevokeEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : RevokeEmployeeInvitation {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, id: Uuid): Result<Unit> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.OWNER)
            val invitation = invitationDataSource.getInvitation(businessId, id) ?: throw Error.NotFound()
            if (invitation.status != EmployeeInvitationStatus.PENDING) {
                throw RevokeEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
            if (!invitationDataSource.revokeInvitation(id)) {
                throw RevokeEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
        }
}
