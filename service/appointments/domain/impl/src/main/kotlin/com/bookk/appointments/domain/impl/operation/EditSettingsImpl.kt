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
    ): Result<AppointmentSettings> = transactionManager.transaction {
        permissionsSource.getPermissions(userId, settings.businessId).assert(ObjectPermission.EDIT)
        if (settings.schedule.list().any { it.isActive && it.workingTime.isEmpty() }) {
            throw EditSettings.Error.ActiveDayWithoutWorkHours()
        }
        if (settings.dayOffs.any { it.start >= it.end }) {
            throw EditSettings.Error.InvalidDayOffRange()
        }
        settingsSource.update(settings)
    }
}