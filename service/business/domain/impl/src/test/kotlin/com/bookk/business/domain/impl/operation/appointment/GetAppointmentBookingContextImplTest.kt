package com.bookk.business.domain.impl.operation.appointment

import com.bookk.business.domain.api.appointment.operation.GetAppointmentBookingContext
import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetAppointmentBookingContextImplTest {

    private class SutFixture {
        val employeeDataSource = mockk<EmployeeDataSource>()
        val clientDataSource = mockk<ClientDataSource>()
        val serviceDataSource = mockk<ServiceDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetAppointmentBookingContextImpl(employeeDataSource, clientDataSource, serviceDataSource, transactionManager)
    }

    @Test
    fun `should return booking context when employee, client and services all exist`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val employee = Employee.stub(id = employeeId, businessId = businessId)
        val client = Client.Detached(id = clientId, name = "Alice", lastName = "Smith", phone = "123", email = "a@b.com")
        val service = Service.stub(businessId = businessId)
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns employee
            coEvery { clientDataSource.getClientById(businessId, clientId) } returns client
            coEvery { serviceDataSource.getServicesByIds(listOf(service.id)) } returns listOf(service)
        }

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, listOf(service.id))

        then()
        assertTrue(result.isSuccess)
        val context = result.getOrNull()!!
        assertEquals(employee, context.employee)
        assertEquals(client.id, context.client.id)
        assertEquals(client.name, context.client.name)
        assertEquals(client.lastName, context.client.lastName)
        assertEquals(client.phone, context.client.phone)
        assertEquals(client.email, context.client.email)
        assertEquals(listOf(service), context.services)
    }

    @Test
    fun `should expand a repeated service id into one context line per requested count`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val employee = Employee.stub(id = employeeId, businessId = businessId)
        val client = Client.Detached(id = clientId, name = "Alice", lastName = "Smith", phone = "123", email = "a@b.com")
        val serviceX = Service.stub(businessId = businessId)
        val serviceIds = listOf(serviceX.id, serviceX.id, serviceX.id, serviceX.id, serviceX.id)
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns employee
            coEvery { clientDataSource.getClientById(businessId, clientId) } returns client
            coEvery { serviceDataSource.getServicesByIds(listOf(serviceX.id)) } returns listOf(serviceX)
        }

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, serviceIds)

        then()
        assertTrue(result.isSuccess)
        val context = result.getOrNull()!!
        assertEquals(List(5) { serviceX }, context.services)
    }

    @Test
    fun `should return EmptyServiceList when service ids list is empty`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val fixture = SutFixture()

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, emptyList())

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GetAppointmentBookingContext.Error.EmptyServiceList)
        coVerify(exactly = 0) { fixture.employeeDataSource.getEmployee(any(), any()) }
    }

    @Test
    fun `should return failure when employee not found`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns null
        }

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, listOf(Uuid.random()))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GetAppointmentBookingContext.Error.EmployeeNotFound)
    }

    @Test
    fun `should return failure when client not found`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val employee = Employee.stub(id = employeeId, businessId = businessId)
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns employee
            coEvery { clientDataSource.getClientById(businessId, clientId) } returns null
        }

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, listOf(Uuid.random()))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GetAppointmentBookingContext.Error.ClientNotFound)
    }

    @Test
    fun `should return failure when some services are not found`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val employee = Employee.stub(id = employeeId, businessId = businessId)
        val client = Client.Detached(id = clientId, name = "Alice", lastName = "Smith", phone = "123", email = "a@b.com")
        val serviceIds = listOf(Uuid.random(), Uuid.random())
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns employee
            coEvery { clientDataSource.getClientById(businessId, clientId) } returns client
            coEvery { serviceDataSource.getServicesByIds(serviceIds) } returns listOf(Service.stub(businessId = businessId))
        }

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, serviceIds)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GetAppointmentBookingContext.Error.ServiceNotFound)
    }

    @Test
    fun `should return failure when a resolved service belongs to another business`() = runUnitTest {
        given()
        val businessId = Uuid.random()
        val employeeId = Uuid.random()
        val clientId = Uuid.random()
        val employee = Employee.stub(id = employeeId, businessId = businessId)
        val client = Client.Detached(id = clientId, name = "Alice", lastName = "Smith", phone = "123", email = "a@b.com")
        val foreignService = Service.stub(businessId = Uuid.random())
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns employee
            coEvery { clientDataSource.getClientById(businessId, clientId) } returns client
            coEvery { serviceDataSource.getServicesByIds(listOf(foreignService.id)) } returns listOf(foreignService)
        }

        whenn()
        val result = fixture.sut.invoke(businessId, employeeId, clientId, listOf(foreignService.id))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GetAppointmentBookingContext.Error.ServiceNotFound)
    }
}
