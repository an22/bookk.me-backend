package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
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

internal class SyncEmployeePermissionTest {

    private class SutFixture {
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val appointmentPermissionDataSource = mockk<AppointmentPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = SyncEmployeePermission(subscriptionDataSource, appointmentPermissionDataSource, transactionManager)
    }

    @Test
    fun `should write the permission when appointments are enabled for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { subscriptionDataSource.isBusinessEnabled(businessId) } returns true
            coEvery { appointmentPermissionDataSource.setPermissions(userId, businessId, ObjectPermission.READ.int) } returns Unit
        }

        whenn()
        val result = fixture.sut.invoke(userId, businessId, ObjectPermission.READ.int)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.appointmentPermissionDataSource.setPermissions(userId, businessId, ObjectPermission.READ.int) }
    }

    @Test
    fun `should skip writing the permission when appointments are not enabled for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { subscriptionDataSource.isBusinessEnabled(businessId) } returns false
        }

        whenn()
        val result = fixture.sut.invoke(userId, businessId, ObjectPermission.EDIT.int)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { fixture.appointmentPermissionDataSource.setPermissions(any(), any(), any()) }
    }
}
