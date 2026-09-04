package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.GetEmployeePermissions
import com.bookk.business.domain.api.employee.operation.GetEmployees
import com.bookk.business.domain.api.employee.operation.SetEmployeePermission
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
import library.permissions.ResourcePermission
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

    private fun ApplicationTestBuilder.authenticatedApplication(useCase: SetEmployeePermission) = setupApplication(
        extension = jwtAuthentication(),
        diModule = module { single { useCase } },
        routeUnderTest = { employeeCrud() }
    )

    private fun ApplicationTestBuilder.authenticatedApplication(useCase: GetEmployeePermissions) = setupApplication(
        extension = jwtAuthentication(),
        diModule = module { single { useCase } },
        routeUnderTest = { employeeCrud() }
    )

    private fun ApplicationTestBuilder.authenticatedApplication(useCase: GetEmployees) = setupApplication(
        extension = jwtAuthentication(),
        diModule = module { single { useCase } },
        routeUnderTest = { employeeCrud() }
    )

    private fun permissionResource(id: Uuid, resource: BusinessResource) = BusinessRouting.Api.Employee.Id.Permission(
        BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), id),
        resource
    )

    private fun permissionsResource(id: Uuid) = BusinessRouting.Api.Employee.Id.Permissions(
        BusinessRouting.Api.Employee.Id(BusinessRouting.Api.Employee(businessId = businessId), id)
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
    fun `should set employee permission`() = routeTest {
        given()
        val useCase: SetEmployeePermission = mockk()
        val id = Uuid.random()
        val permission = ResourcePermission(view = true, update = true)
        val updated = BusinessPermissions.stub(clients = permission)
        coEvery { useCase.invoke(userId, businessId, id, BusinessResource.CLIENTS, permission) } returns Result.success(updated)
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(permissionResource(id, BusinessResource.CLIENTS)) {
            setBody(permission)
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(updated, response.body<BusinessPermissions>())
    }

    @Test
    fun `should return unprocessable entity when caller cannot grant a permission level they do not hold`() = routeTest {
        given()
        val useCase: SetEmployeePermission = mockk()
        val id = Uuid.random()
        val permission = ResourcePermission.FULL
        coEvery { useCase.invoke(userId, businessId, id, BusinessResource.CLIENTS, permission) } returns
            Result.failure(SetEmployeePermission.Error.InsufficientGrant())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(permissionResource(id, BusinessResource.CLIENTS)) {
            setBody(permission)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_INSUFFICIENT_GRANT_PERMISSION, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return not found when setting permission for an unknown employee`() = routeTest {
        given()
        val useCase: SetEmployeePermission = mockk()
        val id = Uuid.random()
        val permission = ResourcePermission(view = true)
        coEvery { useCase.invoke(userId, businessId, id, BusinessResource.CLIENTS, permission) } returns
            Result.failure(Error.NotFound())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(permissionResource(id, BusinessResource.CLIENTS)) {
            setBody(permission)
        }

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return not found when caller has no rights to manage permissions`() = routeTest {
        given()
        val useCase: SetEmployeePermission = mockk()
        val id = Uuid.random()
        val permission = ResourcePermission(view = true)
        coEvery { useCase.invoke(userId, businessId, id, BusinessResource.CLIENTS, permission) } returns
            Result.failure(Error.OperationNotAllowed())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.put(permissionResource(id, BusinessResource.CLIENTS)) {
            setBody(permission)
        }

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unauthorized when setting permission without authentication`() = routeTest {
        given()
        val useCase: SetEmployeePermission = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(permissionResource(Uuid.random(), BusinessResource.CLIENTS)) {
            setBody(ResourcePermission(view = true))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should get employee permissions`() = routeTest {
        given()
        val useCase: GetEmployeePermissions = mockk()
        val id = Uuid.random()
        val permissions = BusinessPermissions.stub(clients = ResourcePermission.FULL)
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.success(permissions)
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource(id))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(permissions, response.body<BusinessPermissions>())
    }

    @Test
    fun `should return not found when getting permissions for an unknown employee`() = routeTest {
        given()
        val useCase: GetEmployeePermissions = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.NotFound())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return not found when caller has no rights to view permissions`() = routeTest {
        given()
        val useCase: GetEmployeePermissions = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(userId, businessId, id) } returns Result.failure(Error.OperationNotAllowed())
        authenticatedApplication(useCase)

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource(id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return unauthorized when getting permissions without authentication`() = routeTest {
        given()
        val useCase: GetEmployeePermissions = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { employeeCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource(Uuid.random()))

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
