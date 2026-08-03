package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.ApproveEmployeeInvitation
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import com.bookk.server.business.client.api.event.BusinessEvent
import library.permissions.ObjectPermission
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class ApproveEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : ApproveEmployeeInvitation {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, id: Uuid): Result<Employee> =
        transactionManager.transaction {
            val invitation = invitationDataSource.getInvitation(businessId, id) ?: throw Error.NotFound()
            if (invitation.userId != requestUserId) throw Error.OperationNotAllowed()
            if (invitation.status != EmployeeInvitationStatus.PENDING) {
                throw ApproveEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
            if (employeeDataSource.getEmployeeByUserId(businessId, invitation.userId) != null) {
                throw ApproveEmployeeInvitation.Error.EmployeeExist()
            }
            if (!invitationDataSource.approveInvitation(id)) {
                throw ApproveEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
            val business = businessDataSource.getBusinessById(businessId) ?: throw Error.NotFound()

            val employee = employeeDataSource.createEmployee(
                Employee(
                    id = Uuid.random(),
                    businessId = businessId,
                    name = invitation.name,
                    lastName = invitation.lastName,
                    phone = invitation.phone,
                    email = invitation.email,
                    userId = invitation.userId,
                    services = emptyList(),
                    createdAt = Clock.System.now()
                )
            )
            businessDataSource.setUserPermissions(invitation.userId, businessId, ObjectPermission.READ.int)
            eventProducer.send(
                BusinessEvent.EmployeeInvitationApproved(
                    inviterUserId = invitation.invitedBy,
                    employeeUserId = employee.userId,
                    employeeName = employee.name,
                    businessId = business.id,
                    businessName = business.name
                )
            )
            employee
        }
}
