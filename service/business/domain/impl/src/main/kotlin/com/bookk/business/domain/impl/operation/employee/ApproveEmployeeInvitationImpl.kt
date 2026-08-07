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
import com.bookk.server.user.client.UserClient
import library.permissions.ObjectPermission
import library.schedule.Schedule
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class ApproveEmployeeInvitationImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val userClient: UserClient,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : ApproveEmployeeInvitation {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, id: Uuid): Result<Employee> =
        transactionManager.transaction {
            val invitation = invitationDataSource.getInvitation(businessId, id) ?: throw Error.NotFound()
            val requestUser = userClient.getUserById(requestUserId).getOrThrow()
            if (invitation.email != requestUser.email) throw Error.OperationNotAllowed()
            if (invitation.status != EmployeeInvitationStatus.PENDING) {
                throw ApproveEmployeeInvitation.Error.InvitationAlreadyProcessed()
            }
            if (employeeDataSource.getEmployeeByUserId(businessId, requestUserId) != null) {
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
                    name = requestUser.name,
                    lastName = requestUser.lastName,
                    phone = requestUser.phone,
                    email = requestUser.email,
                    userId = requestUserId,
                    services = emptyList(),
                    schedule = Schedule.empty(),
                    createdAt = Clock.System.now()
                )
            )
            businessDataSource.setUserPermissions(requestUserId, businessId, ObjectPermission.READ.int)
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
