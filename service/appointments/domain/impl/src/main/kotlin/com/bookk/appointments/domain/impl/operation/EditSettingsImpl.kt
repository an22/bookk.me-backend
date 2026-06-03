package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.operation.EditSettings
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class EditSettingsImpl(
    private val settingsSource: AppointmentSettingsDataSource,
    private val permissionsSource: PermissionsDataSource,
    private val transactionManager: TransactionManager
) : EditSettings {
    override suspend fun invoke(
        userId: Uuid,
        settings: AppointmentSettings
    ): Result<Unit> = transactionManager.transaction {
        permissionsSource.getPermissions(userId, settings.businessId).assert(ObjectPermission.WRITE)
        settingsSource.update(settings)
    }
}