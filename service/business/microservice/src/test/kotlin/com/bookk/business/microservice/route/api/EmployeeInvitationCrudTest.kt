package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.ApproveEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitations
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitationsByEmail
import com.bookk.business.domain.api.employee.operation.RejectEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.RevokeEmployeeInvitation
import com.bookk.business.domain.api.error.BusinessErrorCodes
import com.bookk.business.microservice.route.BusinessRouting
import com.bookk.core.domain.entity.Error
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal class EmployeeInvitationCrudTest {

    private val businessId = Uuid.random()
    private val userId = Uuid.random()

    private fun createTestRequest(email: String = "alice@test.com") = EmployeeInvitationRequest(email = email)

    private fun approveResource(id: Uuid) = BusinessRouting.Api.EmployeeInvitation.Approve(
        BusinessRouting.Api.EmployeeInvitation(businessId = businessId),
        id
    )

    private fun rejectResource(id: Uuid) = BusinessRouting.Api.EmployeeInvitation.Reject(
        BusinessRouting.Api.EmployeeInvitation(businessId = businessId),
        id
    )

    private fun revokeResource(id: Uuid) = BusinessRouting.Api.EmployeeInvitation.Revoke(
        BusinessRouting.Api.EmployeeInvitation(businessId = businessId),
        id
    )

    private fun jwtAuthentication(): Application.() -> Unit = {
        install(Authentication) {
            provider {
                authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
            }
        }
    }

    @Test
    fun `should create employee invitation`() = routeTest {
        given()
        val useCase: CreateEmployeeInvitation = mockk()
        val invitation = EmployeeInvitation.stub(businessId = businessId)
        coEvery { useCase.invoke(userId, any()) } returns Result.success(invitation)

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.EmployeeInvitation(businessId = businessId)) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(invitation.id, response.body<EmployeeInvitation>().id)
    }

    @Test
    fun `should return unprocessable entity when invitation already exists`() = routeTest {
        given()
        val useCase: CreateEmployeeInvitation = mockk()
        coEvery { useCase.invoke(userId, any()) } returns
            Result.failure(CreateEmployeeInvitation.Error.InvitationExist())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.EmployeeInvitation(businessId = businessId)) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_EXISTS,
            response.body<SimpleServerError>().errorCode
        )
    }

    @Test
    fun `should return unprocessable entity when invitation validation fails`() = routeTest {
        given()
        val useCase: CreateEmployeeInvitation = mockk()
        coEvery { useCase.invoke(userId, any()) } returns
            Result.failure(CreateEmployeeInvitation.Error.ValidationError())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.EmployeeInvitation(businessId = businessId)) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_VALIDATION_ERROR,
            response.body<SimpleServerError>().errorCode
        )
    }

    @Test
    fun `should return unprocessable entity when invited user is already an employee`() = routeTest {
        given()
        val useCase: CreateEmployeeInvitation = mockk()
        coEvery { useCase.invoke(userId, any()) } returns
            Result.failure(CreateEmployeeInvitation.Error.EmployeeExist())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.EmployeeInvitation(businessId = businessId)) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_EMPLOYEE_EXISTS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return not found when caller has no rights to invite`() = routeTest {
        given()
        val useCase: CreateEmployeeInvitation = mockk()
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(Error.OperationNotAllowed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.EmployeeInvitation(businessId = businessId)) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unauthorized when creating invitation without authentication`() = routeTest {
        given()
        val useCase: CreateEmployeeInvitation = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.EmployeeInvitation(businessId = businessId)) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return pending invitations sent by the caller`() = routeTest {
        given()
        val useCase: GetPendingEmployeeInvitations = mockk()
        val invitations = listOf(EmployeeInvitation.stub(businessId = businessId, invitedBy = userId))
        coEvery { useCase.invoke(userId, businessId) } returns Result.success(invitations)

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.EmployeeInvitation(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(invitations.map { it.id }, response.body<List<EmployeeInvitation>>().map { it.id })
    }

    @Test
    fun `should return unauthorized when listing pending invitations without authentication`() = routeTest {
        given()
        val useCase: GetPendingEmployeeInvitations = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.EmployeeInvitation(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return pending invitations for the requested email`() = routeTest {
        given()
        val useCase: GetPendingEmployeeInvitationsByEmail = mockk()
        val invitations = listOf(EmployeeInvitation.stub(email = "alice@test.com"))
        coEvery { useCase.invoke("alice@test.com") } returns Result.success(invitations)

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.PendingEmployeeInvitations()) {
            setBody(createTestRequest(email = "alice@test.com"))
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(invitations.map { it.id }, response.body<List<EmployeeInvitation>>().map { it.id })
    }

    @Test
    fun `should return unprocessable entity when requested email is invalid`() = routeTest {
        given()
        val useCase: GetPendingEmployeeInvitationsByEmail = mockk()
        coEvery { useCase.invoke("not-an-email") } returns
            Result.failure(GetPendingEmployeeInvitationsByEmail.Error.ValidationError())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.PendingEmployeeInvitations()) {
            setBody(createTestRequest(email = "not-an-email"))
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_VALIDATION_ERROR,
            response.body<SimpleServerError>().errorCode
        )
    }

    @Test
    fun `should return unauthorized when requesting pending invitations by email without authentication`() = routeTest {
        given()
        val useCase: GetPendingEmployeeInvitationsByEmail = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.PendingEmployeeInvitations()) {
            setBody(createTestRequest())
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should approve employee invitation`() = routeTest {
        given()
        val useCase: ApproveEmployeeInvitation = mockk()
        val id = Uuid.random()
        val employee = Employee.stub(businessId = businessId, userId = userId)
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.success(employee)

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(approveResource(id))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(employee.id, response.body<Employee>().id)
    }

    @Test
    fun `should return not found when approving unknown invitation`() = routeTest {
        given()
        val useCase: ApproveEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.NotFound())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(approveResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return not found when approving invitation of another user`() = routeTest {
        given()
        val useCase: ApproveEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.OperationNotAllowed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(approveResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unprocessable entity when invitation is already processed`() = routeTest {
        given()
        val useCase: ApproveEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns
            Result.failure(ApproveEmployeeInvitation.Error.InvitationAlreadyProcessed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(approveResource(id))

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED,
            response.body<SimpleServerError>().errorCode
        )
    }

    @Test
    fun `should return unprocessable entity when approving user is already an employee`() = routeTest {
        given()
        val useCase: ApproveEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns
            Result.failure(ApproveEmployeeInvitation.Error.EmployeeExist())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(approveResource(id))

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_EMPLOYEE_EXISTS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unauthorized when approving invitation without authentication`() = routeTest {
        given()
        val useCase: ApproveEmployeeInvitation = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(approveResource(Uuid.random()))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should reject employee invitation`() = routeTest {
        given()
        val useCase: RejectEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.success(Unit)

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(rejectResource(id))

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return not found when rejecting unknown invitation`() = routeTest {
        given()
        val useCase: RejectEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.NotFound())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(rejectResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return not found when rejecting invitation of another user`() = routeTest {
        given()
        val useCase: RejectEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.OperationNotAllowed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(rejectResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unprocessable entity when rejecting an already processed invitation`() = routeTest {
        given()
        val useCase: RejectEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns
            Result.failure(RejectEmployeeInvitation.Error.InvitationAlreadyProcessed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(rejectResource(id))

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED,
            response.body<SimpleServerError>().errorCode
        )
    }

    @Test
    fun `should return unauthorized when rejecting invitation without authentication`() = routeTest {
        given()
        val useCase: RejectEmployeeInvitation = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(rejectResource(Uuid.random()))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should revoke employee invitation`() = routeTest {
        given()
        val useCase: RevokeEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.success(Unit)

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(revokeResource(id))

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return not found when revoking unknown invitation`() = routeTest {
        given()
        val useCase: RevokeEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.NotFound())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(revokeResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return not found when caller has no rights to revoke invitations`() = routeTest {
        given()
        val useCase: RevokeEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.OperationNotAllowed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(revokeResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unprocessable entity when revoking an already processed invitation`() = routeTest {
        given()
        val useCase: RevokeEmployeeInvitation = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns
            Result.failure(RevokeEmployeeInvitation.Error.InvitationAlreadyProcessed())

        setupApplication(
            extension = jwtAuthentication(),
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(revokeResource(id))

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(
            BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED,
            response.body<SimpleServerError>().errorCode
        )
    }

    @Test
    fun `should return unauthorized when revoking invitation without authentication`() = routeTest {
        given()
        val useCase: RevokeEmployeeInvitation = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeInvitationCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(revokeResource(Uuid.random()))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
