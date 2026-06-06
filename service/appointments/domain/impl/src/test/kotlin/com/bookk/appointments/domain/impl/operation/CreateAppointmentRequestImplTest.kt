package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
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
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CreateAppointmentRequestImplTest {

    private val requestDataSource = mockk<AppointmentRequestDataSource>()
    private val settingsDataSource = mockk<AppointmentSettingsDataSource>()
    private val permissionsDataSource = mockk<PermissionsDataSource>()
    private val createAppointment = mockk<CreateAppointment>()
    private val transactionManager = mockk<TransactionManager>()
    private fun sut() = CreateAppointmentRequestImpl(
        requestDataSource,
        settingsDataSource,
        permissionsDataSource,
        createAppointment,
        transactionManager
    )

    @Test
    fun `should create request successfully when valid request provided`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(
                Uuid.random(),
                "Service Name",
                Uuid.random(),
                Money.of(CurrencyUnit.of("USD"), 10.0),
                30.minutes
            ),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.automaticApproval } returns false
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { requestDataSource.hasOverlapsWith(request) } returns false
        coEvery { requestDataSource.create(request) } returns request

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { requestDataSource.create(request) }
    }

    @Test
    fun `should create request successfully when valid request provided with automatic approval`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(
                Uuid.random(),
                "Service Name",
                Uuid.random(),
                Money.of(CurrencyUnit.of("USD"), 10.0),
                30.minutes
            ),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.automaticApproval } returns true
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { requestDataSource.hasOverlapsWith(request) } returns false
        coEvery { requestDataSource.create(request) } returns request
        coEvery { createAppointment.invoke(userId, request) } returns Result.success(mockk())

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { requestDataSource.create(request) }
        coVerify(exactly = 1) { createAppointment.invoke(userId, request) }
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
            service = ServiceSnapshot(
                Uuid.random(),
                "Service Name",
                Uuid.random(),
                Money.of(CurrencyUnit.of("USD"), 10.0),
                30.minutes
            ),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.automaticApproval } returns false
        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns true
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { requestDataSource.hasOverlapsWith(request) } returns false

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when create request fails`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot(Uuid.random(), "Client Name", "phone", "client@example.com"),
            service = ServiceSnapshot(
                Uuid.random(),
                "Service Name",
                Uuid.random(),
                Money.of(CurrencyUnit.of("USD"), 10.0),
                30.minutes
            ),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.automaticApproval } returns true
        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns true
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { requestDataSource.hasOverlapsWith(request) } returns false
        coEvery { createAppointment.invoke(userId, request) } returns Result.failure(mockk())

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
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
            service = ServiceSnapshot(
                Uuid.random(),
                "Service Name",
                Uuid.random(),
                Money.of(CurrencyUnit.of("USD"), 10.0),
                30.minutes
            ),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.automaticApproval } returns false
        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns true
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { requestDataSource.hasOverlapsWith(request) } returns false

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed)
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
            service = ServiceSnapshot(
                Uuid.random(),
                "Service Name",
                Uuid.random(),
                Money.of(CurrencyUnit.of("USD"), 10.0),
                30.minutes
            ),
            date = Instant.fromEpochMilliseconds(0),
            note = "Note"
        )
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.automaticApproval } returns false
        coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
        coEvery { settings.isInWorkday(request.date) } returns false
        coEvery { settings.isInWorktime(request.date) } returns false
        coEvery { permissionsDataSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        coEvery { requestDataSource.hasOverlapsWith(request) } returns false
        coEvery { requestDataSource.create(request) } returns request.copy(id = Uuid.random())

        whenn()
        val result = sut().invoke(userId, request)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }
}
