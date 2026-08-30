package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeRole
import com.bookk.business.domain.api.employee.operation.GetEmployees
import com.bookk.business.domain.api.employee.operation.PromoteEmployee
import com.bookk.business.domain.api.employee.operation.UpdateEmployee
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
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal class EmployeeCrudTest {

    private val businessId = Uuid.random()
    private val userId = Uuid.random()

    private fun createTestEmployee() = Employee.stub(businessId = businessId)

    private fun jwtAuthentication(): Application.() -> Unit = {
        install(Authentication) {
            provider {
                authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
            }
        }
    }

    private fun ApplicationTestBuilder.authenticatedApplication(useCase: UpdateEmployee) = setupApplication(
        extension = jwtAuthentication(),
        diModule = module { single { useCase } },
        routeUnderTest = { employeeCrud() }
    )

    private fun ApplicationTestBuilder.authenticatedApplication(useCase: PromoteEmployee) = setupApplication(
        extension = jwtAuthentication(),
        diModule = module { single { useCase } },
        routeUnderTest = { employeeCrud() }
    )

    private fun ApplicationTestBuilder.authenticatedApplication(useCase: GetEmployees) = setupApplication(
        extension = jwtAuthentication(),
        diModule = module { single { useCase } },
        routeUnderTest = { employeeCrud() }
    )

    private fun promoteResource(id: Uuid) = BusinessRouting.Api.Employee.Promote(
        BusinessRouting.Api.Employee(businessId = businessId),
        id
    )

    @Test
    fun `should update employee`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        coEvery { useCase.invoke(userId, employee) } returns Result.success(employee)
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return bad request when path business id does not match body business id`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = Uuid.random()), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `should return bad request when path id does not match body id`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), Uuid.random())) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `should return unprocessable entity when employee validation fails`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        coEvery { useCase.invoke(userId, employee) } returns Result.failure(UpdateEmployee.Error.ValidationError())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_EMPLOYEE_VALIDATION_ERROR, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when active day has no work hours`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        coEvery { useCase.invoke(userId, employee) } returns Result.failure(UpdateEmployee.Error.ActiveDayWithoutWorkHours())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_EMPLOYEE_ACTIVE_DAY_WITHOUT_WORK_HOURS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when day off range is invalid`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        coEvery { useCase.invoke(userId, employee) } returns Result.failure(UpdateEmployee.Error.InvalidDayOffRange())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_EMPLOYEE_INVALID_DAY_OFF_RANGE, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return not found when caller has no permission to edit the employee`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()
        coEvery { useCase.invoke(userId, employee) } returns Result.failure(Error.OperationNotAllowed())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unauthorized when updating employee without authentication`() = routeTest {
        given()
        val useCase: UpdateEmployee = mockk()
        val employee = createTestEmployee()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), employee.id)) {
            setBody(employee)
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should promote employee`() = routeTest {
        given()
        val useCase: PromoteEmployee = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id, EmployeeRole.MANAGER) } returns Result.success(Unit)
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.post(promoteResource(id)) {
            setBody(PromoteEmployeeRequest(role = EmployeeRole.MANAGER))
        }

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return not found when promoting unknown employee`() = routeTest {
        given()
        val useCase: PromoteEmployee = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id, EmployeeRole.EMPLOYEE) } returns
            Result.failure(Error.NotFound())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.post(promoteResource(id)) {
            setBody(PromoteEmployeeRequest(role = EmployeeRole.EMPLOYEE))
        }

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return not found when caller has no rights to promote employees`() = routeTest {
        given()
        val useCase: PromoteEmployee = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id, EmployeeRole.MANAGER) } returns
            Result.failure(Error.OperationNotAllowed())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.post(promoteResource(id)) {
            setBody(PromoteEmployeeRequest(role = EmployeeRole.MANAGER))
        }

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unauthorized when promoting employee without authentication`() = routeTest {
        given()
        val useCase: PromoteEmployee = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(promoteResource(Uuid.random())) {
            setBody(PromoteEmployeeRequest(role = EmployeeRole.MANAGER))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return employees of the business`() = routeTest {
        given()
        val useCase: GetEmployees = mockk()
        val employees = listOf(createTestEmployee(), createTestEmployee())
        coEvery { useCase.invoke(userId, businessId) } returns Result.success(employees)
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Employee(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(employees.map { it.id }, response.body<List<Employee>>().map { it.id })
    }

    @Test
    fun `should return not found when caller has no rights to list employees`() = routeTest {
        given()
        val useCase: GetEmployees = mockk()
        coEvery { useCase.invoke(userId, businessId) } returns Result.failure(Error.OperationNotAllowed())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Employee(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unauthorized when listing employees without authentication`() = routeTest {
        given()
        val useCase: GetEmployees = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Employee(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
