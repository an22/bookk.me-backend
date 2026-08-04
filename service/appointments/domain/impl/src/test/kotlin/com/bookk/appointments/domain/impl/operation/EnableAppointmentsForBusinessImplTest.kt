package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.appointments.domain.api.entity.WorkingSchedule
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EnableAppointmentsForBusinessImplTest {

    private class SutFixture {
        val subscriptionSource = mockk<AppointmentSubscriptionDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val permissionsDataSource = mockk<PermissionsDataSource>()
        val transactionManager = mockk<TransactionManager>()

        val sut = EnableAppointmentsForBusinessImpl(
            subscriptionSource,
            settingsDataSource,
            permissionsDataSource,
            transactionManager
        )
    }

    private val testUserId = Uuid.random()
    private val testBusinessId = Uuid.random()
    private val testSnapshot = BusinessSnapshot.stub(id = testBusinessId)

    @Test
    fun `should enable appointments successfully`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()

        coEvery { fixture.subscriptionSource.attachBusiness(testSnapshot) } returns Unit
        coEvery { fixture.permissionsDataSource.initPermissions(testUserId, testBusinessId, ObjectPermission.OWNER.int) } returns Unit
        coEvery { fixture.settingsDataSource.create(any()) } returns AppointmentSettings.stub(testBusinessId)

        whenn()
        val result = fixture.sut.invoke(testUserId, testSnapshot)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when already enabled`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()

        // Assume transaction fails with constraint violation to trigger AlreadyEnabled error
        coEvery { fixture.subscriptionSource.attachBusiness(testSnapshot) } throws Error.UniqueConstraintFailed("Constraint failure", Exception())

        whenn()
        val result = fixture.sut.invoke(testUserId, testSnapshot)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EnableAppointmentsForBusiness.Error.AlreadyEnabled)
    }

    @Test
    fun `should seed the business replica with the schedule`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val schedule = WorkingSchedule(
            workingDays = listOf(DayOfWeek.SATURDAY),
            workingHours = mapOf(
                DayOfWeek.SATURDAY to listOf(WorkHour(DayOfWeek.SATURDAY, LocalTime(10, 0), LocalTime(14, 0)))
            )
        )
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31)))
        val snapshot = BusinessSnapshot.stub(id = testBusinessId, schedule = schedule, dayOffs = dayOffs)
        coEvery { fixture.subscriptionSource.attachBusiness(any()) } returns Unit
        coEvery { fixture.permissionsDataSource.initPermissions(testUserId, testBusinessId, ObjectPermission.OWNER.int) } returns Unit
        coEvery { fixture.settingsDataSource.create(any()) } returns AppointmentSettings.stub(testBusinessId)

        whenn()
        val result = fixture.sut.invoke(testUserId, snapshot)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.subscriptionSource.attachBusiness(
                match { it.id == testBusinessId && it.schedule == schedule && it.dayOffs == dayOffs }
            )
        }
    }
}
