package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CreateAppointmentImplTest {

    private val futureDate = Instant.parse("2099-01-01T00:00:00Z")

    private class SutFixture {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val requestDataSource = mockk<AppointmentRequestDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val permissionsDataSource = mockk<PermissionsDataSource>()
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>()

        val sut = CreateAppointmentImpl(
            appointmentDataSource,
            requestDataSource,
            settingsDataSource,
            permissionsDataSource,
            subscriptionDataSource,
            transactionManager,
            eventProducer
        )
    }

    @Test
    fun `should create appointment successfully`() = runUnitTest {
        given()

        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = request.businessId, date = request.date)
        val settings = mockk<AppointmentSettings>()
        val sutFixture = SutFixture()

        with(sutFixture) {
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { settingsDataSource.getForUpdate(request.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, request.businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(request.businessId) } returns mockk(relaxed = true)
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = sutFixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        assertEquals(appointment, result.getOrNull())
    }

    @Test
    fun `should return failure when request overlaps with existing appointment`() = runUnitTest {
        given()

        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = request.businessId, date = request.date)
        val settings = mockk<AppointmentSettings>()
        val sutFixture = SutFixture()

        with(sutFixture) {
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { settingsDataSource.getForUpdate(request.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, request.businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns true
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(request.businessId) } returns mockk(relaxed = true)
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = sutFixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.AppointmentForThisTimeExists)
    }

    @Test
    fun `should return failure when date is not in workday`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = request.businessId, date = request.date)
        val settings = mockk<AppointmentSettings>()
        val sutFixture = SutFixture()

        with(sutFixture) {
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns false
            coEvery { settingsDataSource.getForUpdate(request.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, request.businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns true
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(request.businessId) } returns mockk(relaxed = true)
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = sutFixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when request date is in the past`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val pastDate = Instant.parse("2000-01-01T00:00:00Z")
        val request = AppointmentRequest.stub(userId = userId, date = pastDate)
        val settings = mockk<AppointmentSettings>()
        val sutFixture = SutFixture()

        with(sutFixture) {
            coEvery { settingsDataSource.getForUpdate(request.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, request.businessId) } returns EDIT.int
            transactionManager.mockTransaction()
        }

        whenn()
        val result = sutFixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.DateInThePastNotAllowed)
    }

    @Test
    fun `should return failure when time is not in worktime`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = request.businessId, date = request.date)
        val settings = mockk<AppointmentSettings>()
        val sutFixture = SutFixture()

        with(sutFixture) {
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns false
            coEvery { settingsDataSource.getForUpdate(request.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, request.businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns true
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(request.businessId) } returns mockk(relaxed = true)
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = sutFixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.RequestForThisTimeNotAllowed)
    }

    @Test
    fun `should return failure when READ permissions`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = request.businessId, date = request.date)
        val settings = mockk<AppointmentSettings>()
        val sutFixture = SutFixture()

        with(sutFixture) {
            coEvery { settings.isInWorkday(request.date) } returns false
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns false
            coEvery { settingsDataSource.getForUpdate(request.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, request.businessId) } returns READ.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(request.businessId) } returns mockk(relaxed = true)
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = sutFixture.sut.invoke(userId, request)

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
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = businessId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(appointment.date) } returns true
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns true
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } answers { throw RuntimeException("Producer fail") }
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        kotlin.test.assertEquals("Producer fail", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should send request approved event if appointment created`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val appointment = Appointment.stub(userId = userId, businessId = businessId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(appointment.date) } returns true
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns true
            coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.create(request) } returns appointment
            coEvery { requestDataSource.approve(request) } returns Unit
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.eventProducer.send(any(AppointmentEvent.RequestApproved::class), any()) }
    }

    // invoke(userId, appointment: Appointment)

    @Test
    fun `should create instant appointment successfully`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val appointment = Appointment.stub(userId = userId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns settings
            coEvery { settings.isInWorkday(appointment.date) } returns true
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns true
            coEvery { permissionsDataSource.getPermissions(userId, appointment.businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(appointment) } returns false
            coEvery { appointmentDataSource.create(appointment) } returns appointment
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment)

        then()
        assertTrue(result.isSuccess)
        assertEquals(appointment, result.getOrNull())
    }

    @Test
    fun `should return failure when instant appointment overlaps with existing appointment`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val appointment = Appointment.stub(userId = userId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns settings
            coEvery { settings.isInWorkday(appointment.date) } returns true
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns true
            coEvery { permissionsDataSource.getPermissions(userId, appointment.businessId) } returns EDIT.int
            coEvery { appointmentDataSource.hasOverlapsWith(appointment) } returns true
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.AppointmentForThisTimeExists)
    }

    @Test
    fun `should return failure when instant appointment date not allowed`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val appointment = Appointment.stub(userId = userId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns settings
            coEvery { settings.isInWorkday(appointment.date) } returns false
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, appointment.businessId) } returns EDIT.int
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when instant appointment date is in the past`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val pastDate = Instant.parse("2000-01-01T00:00:00Z")
        val appointment = Appointment.stub(userId = userId, date = pastDate)
        val settings = mockk<AppointmentSettings>()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns settings
            coEvery { permissionsDataSource.getPermissions(userId, appointment.businessId) } returns EDIT.int
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.DateInThePastNotAllowed)
    }

    @Test
    fun `should return failure when instant appointment time not allowed`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val appointment = Appointment.stub(userId = userId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns settings
            coEvery { settings.isInWorkday(appointment.date) } returns true
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, appointment.businessId) } returns EDIT.int
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.RequestForThisTimeNotAllowed)
    }

    @Test
    fun `should return failure when instant appointment created with READ permissions`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val appointment = Appointment.stub(userId = userId, date = futureDate)
        val settings = mockk<AppointmentSettings>()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns settings
            coEvery { settings.isInWorkday(appointment.date) } returns false
            coEvery { settings.isInWorktime(appointment.date, appointment.dateEnd) } returns false
            coEvery { permissionsDataSource.getPermissions(userId, appointment.businessId) } returns READ.int
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }
}
