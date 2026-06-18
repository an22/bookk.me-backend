package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.operation.CancelAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class CancelAppointmentImplTest {

    private class SutFixture {
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val permissionsDataSource = mockk<PermissionsDataSource>()
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val transactionManager = mockk<TransactionManager>()

        val sut = CancelAppointmentImpl(
            appointmentDataSource,
            permissionsDataSource,
            subscriptionDataSource,
            eventProducer,
            transactionManager
        )
    }

    private val testUserId = Uuid.random()
    private val testBusinessId = Uuid.random()
    private val testCancellation = AppointmentCancellation(
        id = Uuid.random(),
        businessId = testBusinessId,
        reason = "User cancelled"
    )

    @Test
    fun `should cancel appointment successfully`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val appointment = Appointment.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentStatus.SCHEDULED)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.appointmentDataSource.get(testCancellation.id) } returns appointment
        coEvery { fixture.appointmentDataSource.cancel(testCancellation.id, testCancellation.reason) } returns appointment.copy(status = AppointmentStatus.CANCELLED)
        coEvery { fixture.subscriptionDataSource.getBusinessSnapshot(testBusinessId) } returns mockk(relaxed = true)

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when appointment already cancelled`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val appointment = Appointment.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentStatus.CANCELLED)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.appointmentDataSource.get(testCancellation.id) } returns appointment

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CancelAppointment.Error.AlreadyCancelled)
    }

    @Test
    fun `should return failure when appointment already completed`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val appointment = Appointment.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentStatus.COMPLETED)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.appointmentDataSource.get(testCancellation.id) } returns appointment

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CancelAppointment.Error.AlreadyCompleted)
    }

    @Test
    fun `should return failure when user has no permissions`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.READ.int

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is com.bookk.core.domain.entity.Error.OperationNotAllowed)
    }

    @Test
    fun `should send appointment cancelled event successfully`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val appointment = Appointment.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentStatus.SCHEDULED)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.appointmentDataSource.get(testCancellation.id) } returns appointment
        coEvery { fixture.appointmentDataSource.cancel(testCancellation.id, testCancellation.reason) } returns appointment.copy(status = AppointmentStatus.CANCELLED)
        coEvery { fixture.subscriptionDataSource.getBusinessSnapshot(testBusinessId) } returns mockk(relaxed = true)

        whenn()
        fixture.sut.invoke(testUserId, testCancellation)

        then()
        coVerify(exactly = 1) { fixture.eventProducer.send(any(AppointmentEvent.Cancelled::class), any()) }
    }
}
