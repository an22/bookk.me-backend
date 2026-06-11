package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ObjectPermission.EDIT
import library.permissions.ObjectPermission.READ
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

internal class CreateAppointmentRequestImplTest {

    private class SutFixture {
        val requestDataSource = mockk<AppointmentRequestDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val permissionsDataSource = mockk<PermissionsDataSource>()
        val createAppointment = mockk<CreateAppointment>()
        val transactionManager = mockk<TransactionManager>()
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val eventProducer = mockk<StandardEventProducer>()

        val sut = CreateAppointmentRequestImpl(
            requestDataSource,
            settingsDataSource,
            permissionsDataSource,
            subscriptionDataSource,
            eventProducer,
            createAppointment,
            transactionManager
        )
    }

    @Test
    fun `should create request successfully when valid request provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(any()) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.requestDataSource.create(request) }
    }

    @Test
    fun `should create request successfully when valid request provided with automatic approval`() = runUnitTest {
        given()

        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns true
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            coEvery { createAppointment.invoke(userId, request) } returns Result.success(mockk())
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { fixture.requestDataSource.create(request) }
        coVerify(exactly = 1) { fixture.createAppointment.invoke(userId, request) }
    }

    @Test
    fun `should return failure when request is in workday`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when create request fails`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns true
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            coEvery { createAppointment.invoke(userId, request) } returns Result.failure(RuntimeException())
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `should return failure when request is in worktime`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns true
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed)
    }

    @Test
    fun `should return failure when READ permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns READ.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should return failure when create event fails to be sent`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } answers { throw RuntimeException("Producer fail") }
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Producer fail", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return failure when request overlaps with existing appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns true
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisTimeExists)
    }

    @Test
    fun `should send event if appointment created`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.eventProducer.send(any(AppointmentEvent.RequestCreated::class), any()) }
    }
}
