package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.EmployeeSnapshot
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ResourcePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class UpdateAppointmentImplTest {

    private class SutFixture {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val settings = mockk<AppointmentSettings>()

        val sut = UpdateAppointmentImpl(
            appointmentDataSource,
            settingsDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )

        fun givenValidUpdate(userId: Uuid, stored: Appointment, permission: ResourcePermission = ResourcePermission(view = false, update = true, delete = false)) {
            transactionManager.mockTransaction()
            coEvery { settingsDataSource.getForUpdate(stored.businessId) } returns settings
            coEvery { appointmentDataSource.getForUpdate(stored.id) } returns stored
            coEvery { settings.isInWorkday(any()) } returns true
            coEvery { settings.isInWorktime(any(), any()) } returns true
            coEvery { appointmentPermissionDataSource.getPermission(userId, stored.businessId) } returns permission
            coEvery { appointmentDataSource.update(any<Appointment>()) } returns stored
            coEvery { appointmentDataSource.hasOverlapsWith(any<Appointment>()) } returns false
        }
    }

    private val futureDate = Instant.parse("2099-01-01T00:00:00Z")

    @Test
    fun `should update appointment successfully`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)

        whenn()
        val result = fixture.sut(userId, appointment)

        then()
        assertTrue(result.isSuccess)
        assertEquals(appointment, result.getOrNull())
    }

    @Test
    fun `should write appointment exactly once`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)

        whenn()
        fixture.sut(userId, appointment)

        then()
        coVerify(exactly = 1) { fixture.appointmentDataSource.update(appointment) }
    }

    @Test
    fun `should return the stored appointment rather than the request`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        val stored = appointment.copy(note = "Stored note")
        fixture.givenValidUpdate(userId, appointment)
        coEvery { fixture.appointmentDataSource.update(any<Appointment>()) } returns stored

        whenn()
        val result = fixture.sut(userId, appointment)

        then()
        assertEquals(stored, result.getOrNull())
    }

    @Test
    fun `should return failure when appointment does not exist (settings not found)`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val appointment = Appointment.stub(date = futureDate)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { settingsDataSource.getForUpdate(appointment.businessId) } returns null
        }

        whenn()
        val result = fixture.sut(Uuid.random(), appointment)

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }

    @Test
    fun `should return failure when user has read permission but appointment belongs to another employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment, ResourcePermission(view = true, update = false, delete = false))

        whenn()
        val result = fixture.sut(userId, appointment)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should update own appointment successfully with read permission`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val ownAppointment = Appointment.stub(date = futureDate).copy(employee = EmployeeSnapshot.stub(userId = userId))
        fixture.givenValidUpdate(userId, ownAppointment, ResourcePermission(view = true, update = false, delete = false))

        whenn()
        val result = fixture.sut(userId, ownAppointment)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should not write appointment when validation fails`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)
        coEvery { fixture.appointmentDataSource.hasOverlapsWith(any<Appointment>()) } returns true

        whenn()
        fixture.sut(userId, appointment)

        then()
        coVerify(exactly = 0) { fixture.appointmentDataSource.update(any()) }
    }

    @Test
    fun `should return failure when overlap exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)
        coEvery { fixture.appointmentDataSource.hasOverlapsWith(any<Appointment>()) } returns true

        whenn()
        val result = fixture.sut(userId, appointment)

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.AppointmentForThisTimeExists)
    }

    @Test
    fun `should return failure when date is in the past`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val pastAppointment = Appointment.stub(date = Instant.parse("2000-01-01T00:00:00Z"))
        fixture.givenValidUpdate(userId, pastAppointment)

        whenn()
        val result = fixture.sut(userId, pastAppointment)

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.DateInThePastNotAllowed)
    }

    @Test
    fun `should return failure when workday not allowed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)
        coEvery { fixture.settings.isInWorkday(any()) } returns false

        whenn()
        val result = fixture.sut(userId, appointment)

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when time not allowed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)
        coEvery { fixture.settings.isInWorktime(any(), any()) } returns false

        whenn()
        val result = fixture.sut(userId, appointment)

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.RequestForThisTimeNotAllowed)
    }

    @Test
    fun `should return failure when appointment is cancelled`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val stored = Appointment.stub(date = futureDate).copy(status = AppointmentStatus.CANCELLED)
        fixture.givenValidUpdate(userId, stored)

        whenn()
        val result = fixture.sut(userId, stored.copy(status = AppointmentStatus.SCHEDULED))

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.AlreadyCancelled)
        coVerify(exactly = 0) { fixture.appointmentDataSource.update(any()) }
    }

    @Test
    fun `should return failure when appointment is completed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val stored = Appointment.stub(date = futureDate).copy(status = AppointmentStatus.COMPLETED)
        fixture.givenValidUpdate(userId, stored)

        whenn()
        val result = fixture.sut(userId, stored.copy(status = AppointmentStatus.SCHEDULED))

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.AlreadyCompleted)
        coVerify(exactly = 0) { fixture.appointmentDataSource.update(any()) }
    }

    @Test
    fun `should return failure when appointment is marked as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val stored = Appointment.stub(date = futureDate).copy(status = AppointmentStatus.NO_SHOW)
        fixture.givenValidUpdate(userId, stored)

        whenn()
        val result = fixture.sut(userId, stored.copy(status = AppointmentStatus.SCHEDULED))

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.MarkedNoShow)
        coVerify(exactly = 0) { fixture.appointmentDataSource.update(any()) }
    }

    @Test
    fun `should return failure when request changes the status`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val stored = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, stored)

        whenn()
        val result = fixture.sut(userId, stored.copy(status = AppointmentStatus.CANCELLED))

        then()
        assertTrue(result.exceptionOrNull() is UpdateAppointment.Error.StatusChangeNotAllowed)
        coVerify(exactly = 0) { fixture.settingsDataSource.getForUpdate(any()) }
        coVerify(exactly = 0) { fixture.appointmentDataSource.getForUpdate(any()) }
        coVerify(exactly = 0) { fixture.appointmentDataSource.update(any()) }
    }

    @Test
    fun `should read the stored appointment with a row lock`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub(date = futureDate)
        fixture.givenValidUpdate(userId, appointment)

        whenn()
        fixture.sut(userId, appointment)

        then()
        coVerify(exactly = 1) { fixture.appointmentDataSource.getForUpdate(appointment.id) }
        coVerify(exactly = 0) { fixture.appointmentDataSource.get(any()) }
    }
}
