package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.Business
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
import com.bookk.server.user.client.UserClient
import library.permissions.ObjectPermission
import library.permissions.assert
import library.validation.EmailValidator
import kotlin.uuid.Uuid
import com.bookk.core.domain.entity.Error as InfrastructureError

internal class CreateEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val userClient: UserClient,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : CreateEmployeeInvitation {
    override suspend fun invoke(
        requestUserId: Uuid,
        invitation: EmployeeInvitation
    ): Result<EmployeeInvitation> {
        if (!EmailValidator.isValid(invitation.email)) {
            return Result.failure(Error.ValidationError())
        }
        return transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, invitation.businessId).assert(ObjectPermission.OWNER)
            if (employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email) != null) {
                throw Error.EmployeeExist()
            }
            val business = businessDataSource.getBusinessById(invitation.businessId) ?: throw InfrastructureError.NotFound()

            invitationDataSource.createInvitation(invitation.copy(invitedBy = requestUserId)).also { created ->
                sendInviteEvent(created, business)
            }
        }.onConstraintFailure {
            throw Error.InvitationExist()
        }
    }

    private suspend fun sendInviteEvent(created: EmployeeInvitation, business: Business) {
        userClient.getUserByEmail(created.email).onSuccess { invitedUser ->
            eventProducer.send(
                BusinessEvent.EmployeeInvitationCreated(
                    invitedUserId = invitedUser.id,
                    businessId = business.id,
                    businessName = business.name
                )
            )
        }
    }
}
