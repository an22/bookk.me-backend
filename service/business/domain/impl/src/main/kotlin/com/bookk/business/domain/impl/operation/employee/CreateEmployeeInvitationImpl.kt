package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class CreateEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val businessDataSource: BusinessDataSource,
    private val businessPermissionDataSource: BusinessPermissionDataSource,
    private val transactionManager: TransactionManager
) : CreateEmployeeInvitation {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid): Result<EmployeeInvitation> {
        return transactionManager.transaction {
            businessPermissionDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.OWNER)
            businessDataSource.getBusinessById(businessId) ?: throw Error.NotFound()
            createInvitationWithUniqueCode(requestUserId, businessId)
        }
    }

    private suspend fun createInvitationWithUniqueCode(
        requestUserId: Uuid,
        businessId: Uuid,
        remainingAttempts: Int = MAX_CODE_ATTEMPTS
    ): EmployeeInvitation {
        val plainCode = EmployeeInvitationCode.generate()
        val invitation = EmployeeInvitation(
            id = Uuid.random(),
            businessId = businessId,
            invitedBy = requestUserId,
            code = EmployeeInvitationCode.hash(plainCode),
            status = EmployeeInvitationStatus.PENDING,
            createdAt = Clock.System.now()
        )
        return try {
            invitationDataSource.createInvitation(invitation).copy(code = plainCode)
        } catch (error: Error.UniqueConstraintFailed) {
            if (remainingAttempts <= 1) throw error
            createInvitationWithUniqueCode(requestUserId, businessId, remainingAttempts - 1)
        }
    }

    private companion object {
        const val MAX_CODE_ATTEMPTS = 5
    }
}
