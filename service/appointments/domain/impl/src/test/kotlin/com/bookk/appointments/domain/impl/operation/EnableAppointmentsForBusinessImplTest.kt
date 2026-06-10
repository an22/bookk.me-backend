package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
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
import io.mockk.mockk
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
    private val testSnapshot = BusinessSnapshot.stub().copy(id = testBusinessId)

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
}
