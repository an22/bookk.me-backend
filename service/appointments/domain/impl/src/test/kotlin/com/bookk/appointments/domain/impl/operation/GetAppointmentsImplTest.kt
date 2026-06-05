package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
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

internal class GetAppointmentsImplTest {

    private val appointmentDataSource = mockk<AppointmentDataSource>()
    private val permissionsDataSource = mockk<PermissionsDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = GetAppointmentsImpl(
        appointmentDataSource,
        permissionsDataSource,
        transactionManager
    )

    @Test
    fun `should return appointments when user has read permissions`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val appointments = listOf(
            Appointment(
                id = Uuid.random(),
                userId = userId,
                businessId = businessId,
                client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
                service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.of(CurrencyUnit.of("USD"), 10.0), 30.minutes),
                date = Instant.fromEpochMilliseconds(0),
                note = "Note"
            )
        )

        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        coEvery { appointmentDataSource.getAll(businessId) } returns appointments

        whenn()
        val result = sut(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(appointments, result.getOrNull())
    }

    @Test
    fun `should return failure when user has no permissions`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()

        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns null

        whenn()
        val result = sut(userId, businessId)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should return failure when data source fails`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val exception = RuntimeException("Database error")

        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        coEvery { appointmentDataSource.getAll(businessId) } throws exception

        whenn()
        val result = sut(userId, businessId)

        then()
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
