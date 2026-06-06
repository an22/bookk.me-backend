package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ObjectPermission.OWNER
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EnableAppointmentsForBusinessImplTest {


    private class Fixture {
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

    @Test
    fun `should enable appointments for business successfully`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val snapshot = BusinessSnapshot.stub()
        val fixture = Fixture()

        with(fixture) {
            coEvery { subscriptionSource.attachBusiness(snapshot) } returns Unit
            coEvery { permissionsDataSource.initPermissions(userId, snapshot.id, OWNER.int) } returns Unit
            coEvery { settingsDataSource.create(any()) } returns mockk()
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, snapshot)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.subscriptionSource.attachBusiness(snapshot) }
        coVerify(exactly = 1) { fixture.permissionsDataSource.initPermissions(userId, snapshot.id, eq(OWNER.int)) }
        coVerify(exactly = 1) { fixture.settingsDataSource.create(any()) }
    }
}
