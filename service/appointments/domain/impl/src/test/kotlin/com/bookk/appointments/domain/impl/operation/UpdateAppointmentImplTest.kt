package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.EmployeeSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class UpdateAppointmentImplTest {

    private val testUserId = Uuid.random()
    private val testBusinessId = Uuid.random()
    private val testAppointment = Appointment(
        id = Uuid.random(),
        userId = Uuid.random(),
        businessId = testBusinessId,
        employee = EmployeeSnapshot.stub(),
        client = ClientSnapshot(Uuid.random(), "Name", "123", "a@b.com"),
        services = listOf(ServiceSnapshot(Uuid.random(), "Svc", Uuid.random(), Money.of(CurrencyUnit.USD, 10.0), 30.minutes)),
        date = Instant.parse("2099-01-01T00:00:00Z"),
        note = "Note",
        status = AppointmentStatus.SCHEDULED,
        cancellationReason = ""
    )

    @Test
    fun `should update appointment successfully`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(testAppointment.id) } returns testAppointment
        coEvery { settings.isInWorkday(any()) } returns true
        coEvery { settings.isInWorktime(any(), any()) } returns true
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { appointmentDataSource.update(any<Appointment>()) } returns testAppointment
        coEvery { appointmentDataSource.hasOverlapsWith(any<Appointment>()) } returns false

        whenn()
        val result = sut(testUserId, testAppointment)

        then()
        assertTrue(result.isSuccess)
        assertEquals(testAppointment, result.getOrNull())
    }

    @Test
    fun `should return failure when appointment does not exist (settings not found)`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns null

        whenn()
        val result = sut(testUserId, testAppointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is com.bookk.core.domain.entity.Error.NotFound)
    }

    @Test
    fun `should return failure when user has read permission but appointment belongs to another employee`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(testAppointment.id) } returns testAppointment
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.READ.int

        whenn()
        val result = sut(testUserId, testAppointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is com.bookk.core.domain.entity.Error.OperationNotAllowed)
    }

    @Test
    fun `should update own appointment successfully with read permission`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()
        val ownAppointment = testAppointment.copy(employee = EmployeeSnapshot.stub(userId = testUserId))
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(ownAppointment.id) } returns ownAppointment
        coEvery { settings.isInWorkday(any()) } returns true
        coEvery { settings.isInWorktime(any(), any()) } returns true
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.READ.int
        coEvery { appointmentDataSource.update(any<Appointment>()) } returns ownAppointment
        coEvery { appointmentDataSource.hasOverlapsWith(any<Appointment>()) } returns false

        whenn()
        val result = sut(testUserId, ownAppointment)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when overlap exists`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(testAppointment.id) } returns testAppointment
        coEvery { settings.isInWorkday(any()) } returns true
        coEvery { settings.isInWorktime(any(), any()) } returns true
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { appointmentDataSource.update(any<Appointment>()) } returns testAppointment // Update is called BEFORE overlap check
        coEvery { appointmentDataSource.hasOverlapsWith(any<Appointment>()) } returns true

        whenn()
        val result = sut(testUserId, testAppointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.AppointmentForThisTimeExists)
    }

    @Test
    fun `should return failure when date is in the past`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()
        val pastAppointment = testAppointment.copy(date = Instant.parse("2000-01-01T00:00:00Z"))
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(pastAppointment.id) } returns pastAppointment
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int

        whenn()
        val result = sut(testUserId, pastAppointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.DateInThePastNotAllowed)
    }

    @Test
    fun `should return failure when workday not allowed`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(testAppointment.id) } returns testAppointment
        coEvery { settings.isInWorkday(any()) } returns false
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { appointmentDataSource.update(any()) } returns testAppointment // Update is called BEFORE workday check

        whenn()
        val result = sut(testUserId, testAppointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when time not allowed`() = runUnitTest {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        given()
        transactionManager.mockTransaction()
        val settings = mockk<AppointmentSettings>()
        coEvery { settingsDataSource.getForUpdate(testBusinessId) } returns settings
        coEvery { appointmentDataSource.get(testAppointment.id) } returns testAppointment
        coEvery { settings.isInWorkday(any()) } returns true
        coEvery { settings.isInWorktime(any(), any()) } returns false
        coEvery { appointmentPermissionDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { appointmentDataSource.update(any()) } returns testAppointment // Update is called BEFORE worktime check

        whenn()
        val result = sut(testUserId, testAppointment)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.RequestForThisTimeNotAllowed)
    }
}
