package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCompletedBy
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.EmployeeSnapshot
import com.bookk.appointments.domain.api.operation.CompleteAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
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
import io.mockk.slot
import library.permissions.EmployeeAccessSuspended
import library.permissions.ResourcePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CompleteAppointmentImplTest {

    private class SutFixture {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()

        val sut = CompleteAppointmentImpl(
            appointmentDataSource,
            appointmentPermissionDataSource,
            transactionManager
        )
    }

    @Test
    fun `should complete appointment successfully`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub()
        val completed = appointment.copy(status = AppointmentStatus.COMPLETED, completedBy = AppointmentCompletedBy.USER)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = false, update = true, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, any()) } returns completed
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.isSuccess)
        assertEquals(completed, result.getOrNull())
    }

    @Test
    fun `should return already completed appointment unchanged`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub().copy(status = AppointmentStatus.COMPLETED, completedBy = AppointmentCompletedBy.SYSTEM)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = false, update = true, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, any()) } returns appointment
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertEquals(appointment, result.getOrNull())
    }

    @Test
    fun `should only accept appointments that started before now`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub()
        val startedBefore = slot<Instant>()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = false, update = true, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, capture(startedBefore)) } returns appointment.copy(status = AppointmentStatus.COMPLETED)
        }
        val lowerBound = Clock.System.now()

        whenn()
        fixture.sut.invoke(userId, appointment.id)

        then()
        val upperBound = Clock.System.now()
        assertTrue(startedBefore.captured in lowerBound..upperBound)
    }

    @Test
    fun `should complete own appointment with view permission`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub().copy(employee = EmployeeSnapshot.stub(userId = userId))
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = true, update = false, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, any()) } returns appointment.copy(status = AppointmentStatus.COMPLETED)
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when user has view permission but appointment belongs to another employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = true, update = false, delete = false)
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.appointmentDataSource.markCompletedByUser(any(), any()) }
    }

    @Test
    fun `should return failure when caller is a suspended employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } throws EmployeeAccessSuspended()
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.exceptionOrNull() is EmployeeAccessSuspended)
        coVerify(exactly = 0) { fixture.appointmentDataSource.markCompletedByUser(any(), any()) }
    }

    @Test
    fun `should return failure when appointment is cancelled`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub().copy(status = AppointmentStatus.CANCELLED)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = false, update = true, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, any()) } returns appointment
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.exceptionOrNull() is CompleteAppointment.Error.AlreadyCancelled)
    }

    @Test
    fun `should return failure when appointment is marked as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub().copy(status = AppointmentStatus.NO_SHOW)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = false, update = true, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, any()) } returns appointment
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.exceptionOrNull() is CompleteAppointment.Error.MarkedNoShow)
    }

    @Test
    fun `should return failure when appointment has not started yet`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val appointment = Appointment.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointment.id) } returns appointment
            coEvery { appointmentPermissionDataSource.getPermission(userId, appointment.businessId) } returns ResourcePermission(view = false, update = true, delete = false)
            coEvery { appointmentDataSource.markCompletedByUser(appointment.id, any()) } returns appointment
        }

        whenn()
        val result = fixture.sut.invoke(userId, appointment.id)

        then()
        assertTrue(result.exceptionOrNull() is CompleteAppointment.Error.NotStarted)
    }

    @Test
    fun `should return failure when appointment does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val appointmentId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { appointmentDataSource.get(appointmentId) } throws Error.NotFound()
        }

        whenn()
        val result = fixture.sut.invoke(Uuid.random(), appointmentId)

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }
}
