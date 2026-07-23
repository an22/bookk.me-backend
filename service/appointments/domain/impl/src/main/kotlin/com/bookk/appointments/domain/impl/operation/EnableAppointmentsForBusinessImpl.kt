package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.onConstraintFailure
import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

internal class EnableAppointmentsForBusinessImpl(
    private val subscriptionSource: AppointmentSubscriptionDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val transactionManager: TransactionManager
) : EnableAppointmentsForBusiness {
    override suspend fun invoke(userId: Uuid, snapshot: BusinessSnapshot): Result<Unit> = transactionManager.transaction<Unit> {
        subscriptionSource.attachBusiness(snapshot)
        permissionsDataSource.initPermissions(userId, snapshot.id, ObjectPermission.OWNER.int)
        settingsDataSource.create(AppointmentSettings(snapshot.id, snapshot.timeZone))
    }.onConstraintFailure {
        throw EnableAppointmentsForBusiness.Error.AlreadyEnabled()
    }
}