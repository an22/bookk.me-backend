package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EditSettingsImplTest {

    private val settingsSource = mockk<AppointmentSettingsDataSource>()
    private val permissionsSource = mockk<PermissionsDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private fun sut() = EditSettingsImpl(
        settingsSource,
        permissionsSource,
        transactionManager
    )

    @Test
    fun `should update settings successfully when valid settings provided`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.businessId } returns businessId
        coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.WRITE.int
        coEvery { settingsSource.update(settings) } returns settings

        whenn()
        val result = sut().invoke(userId, settings)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return operation not allowed when invalid permissions`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.businessId } returns businessId
        coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        coEvery { settingsSource.update(settings) } returns settings

        whenn()
        val result = sut().invoke(userId, settings)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should return failure on any exception from datasource`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val settings = mockk<AppointmentSettings>()

        coEvery { settings.businessId } returns businessId
        coEvery { permissionsSource.getPermissions(userId, businessId) } returns ObjectPermission.READ.int
        coEvery { settingsSource.update(settings) } answers { throw IllegalStateException() }

        whenn()
        val result = sut().invoke(userId, settings)

        then()
        assertTrue(result.isFailure)
    }
}
