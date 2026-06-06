package com.bookk.appointments.domain.impl.operation

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
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EnableAppointmentsForBusinessImplTest {

    private val subscriptionSource = mockk<AppointmentSubscriptionDataSource>()
    private val settingsDataSource = mockk<AppointmentSettingsDataSource>()
    private val permissionsDataSource = mockk<PermissionsDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private fun sut() = EnableAppointmentsForBusinessImpl(
        subscriptionSource,
        settingsDataSource,
        permissionsDataSource,
        transactionManager
    )

    @Test
    fun `should enable appointments for business successfully`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()

        coEvery { subscriptionSource.attachBusiness(businessId) } returns Unit
        coEvery { permissionsDataSource.initPermissions(userId, businessId, ObjectPermission.OWNER.int) } returns Unit
        coEvery { settingsDataSource.create(any()) } returns mockk()

        whenn()
        val result = sut().invoke(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { subscriptionSource.attachBusiness(businessId) }
        coVerify(exactly = 1) { permissionsDataSource.initPermissions(userId, businessId, eq(ObjectPermission.OWNER.int)) }
        coVerify(exactly = 1) { settingsDataSource.create(any()) }
    }
}
