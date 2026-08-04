package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation.Error
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.onConstraintFailure
import com.bookk.server.business.client.api.event.BusinessEvent
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid
import com.bookk.core.domain.entity.Error as InfrastructureError

internal class CreateEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : CreateEmployeeInvitation {
    override suspend fun invoke(
        requestUserId: Uuid,
        invitation: EmployeeInvitation
    ): Result<EmployeeInvitation> {
        if (invitation.name.isBlank() || invitation.name.length > MAX_NAME_LENGTH) {
            return Result.failure(Error.ValidationError())
        }
        if (invitation.lastName.isBlank() || invitation.lastName.length > MAX_NAME_LENGTH) {
            return Result.failure(Error.ValidationError())
        }
        return transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, invitation.businessId).assert(ObjectPermission.EDIT)
            if (employeeDataSource.getEmployeeByUserId(invitation.businessId, invitation.userId) != null) {
                throw Error.EmployeeExist()
            }
            val business = businessDataSource.getBusinessById(invitation.businessId)
                ?: throw InfrastructureError.NotFound()

            // The inviter is always the authenticated caller, never whatever the request body claimed.
            invitationDataSource.createInvitation(invitation.copy(invitedBy = requestUserId)).also { created ->
                eventProducer.send(
                    BusinessEvent.EmployeeInvitationCreated(
                        invitedUserId = created.userId,
                        invitedName = created.name,
                        businessId = business.id,
                        businessName = business.name
                    )
                )
            }
        }.onConstraintFailure {
            throw Error.InvitationExist()
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 512
    }
}
