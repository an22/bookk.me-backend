package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
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

internal class CreateAppointmentImplTest {

    private val appointmentDataSource = mockk<AppointmentDataSource>()
    private val requestDataSource = mockk<AppointmentRequestDataSource>()
    private val settingsDataSource = mockk<AppointmentSettingsDataSource>()
    private val permissionsDataSource = mockk<PermissionsDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private fun sut() = CreateAppointmentImpl(
        appointmentDataSource,
        requestDataSource,
        settingsDataSource,
        permissionsDataSource,
        transactionManager
    )

    @Test
    fun `should create appointment successfully when valid request provided`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.of(CurrencyUnit.of("USD"), 10.0), 30.minutes),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val appointment = Appointment(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = request.client,
            service = request.service,
            date = request.date,
            note = request.note
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
        coEvery { appointmentDataSource.create(request) } returns appointment

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        assertEquals(appointment, result.getOrNull())
    }

    @Test
    fun `should return failure when request overlaps with existing appointment`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.of(CurrencyUnit.of("USD"), 10.0), 30.minutes),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { appointmentDataSource.hasOverlapsWith(request) } returns true

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.AppointmentForThisTimeExists)
    }

    @Test
    fun `should return failure when request is in workday`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.of(CurrencyUnit.of("USD"), 10.0), 30.minutes),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns true
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when request is in worktime`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.of(CurrencyUnit.of("USD"), 10.0), 30.minutes),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns true
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointment.Error.RequestForThisTimeNotAllowed)
    }

    @Test
    fun `should return failure when READ permissions`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.of(CurrencyUnit.of("USD"), 10.0), 30.minutes),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val appointment = Appointment(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = request.client,
            service = request.service,
            date = request.date,
            note = request.note
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
        coEvery { appointmentDataSource.create(request) } returns appointment

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }
}
