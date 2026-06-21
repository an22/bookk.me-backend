package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.appointments.domain.api.entity.WorkingSchedule
import com.bookk.appointments.domain.api.operation.EditSettings
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EditSettingsImplTest {

    private class SutFixture {
        val settingsSource = mockk<AppointmentSettingsDataSource>()
        val permissionsSource = mockk<PermissionsDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = EditSettingsImpl(settingsSource, permissionsSource, transactionManager)
    }

    @Test
    fun `should update settings successfully when valid settings provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = AppointmentSettings.stub(businessId = businessId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.EDIT.int
            coEvery { settingsSource.update(settings) } returns settings
        }

        whenn()
        val result = fixture.sut.invoke(userId, settings)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return operation not allowed when invalid permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = mockk<AppointmentSettings>()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { settings.businessId } returns businessId
            coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        }

        whenn()
        val result = fixture.sut.invoke(userId, settings)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should return active day without work hours error when active day has no work hours`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = AppointmentSettings.stub(businessId = businessId).copy(
            schedule = WorkingSchedule(workingDays = listOf(DayOfWeek.MONDAY), workingHours = mapOf())
        )
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.EDIT.int
        }

        whenn()
        val result = fixture.sut.invoke(userId, settings)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EditSettings.Error.ActiveDayWithoutWorkHours)
    }

    @Test
    fun `should return invalid day off range error when start date is not before end date`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val date = LocalDate(2026, 6, 22)
        val settings = AppointmentSettings.stub(businessId = businessId).copy(
            dayOffs = listOf(DayOffRange(start = date, end = date))
        )
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.EDIT.int
        }

        whenn()
        val result = fixture.sut.invoke(userId, settings)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EditSettings.Error.InvalidDayOffRange)
    }

    @Test
    fun `should return failure on any exception from datasource`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = AppointmentSettings.stub(businessId = businessId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.EDIT.int
            coEvery { settingsSource.update(settings) } answers { throw IllegalStateException() }
        }

        whenn()
        val result = fixture.sut.invoke(userId, settings)

        then()
        assertTrue(result.isFailure)
    }
}
