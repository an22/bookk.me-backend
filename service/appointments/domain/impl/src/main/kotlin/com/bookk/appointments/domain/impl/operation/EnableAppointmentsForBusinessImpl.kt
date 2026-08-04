package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.onConstraintFailure
import com.bookk.server.business.client.api.BusinessClient
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class EnableAppointmentsForBusinessImpl(
    private val subscriptionSource: AppointmentSubscriptionDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val businessClient: BusinessClient,
    private val transactionManager: TransactionManager
) : EnableAppointmentsForBusiness {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<Unit> {
        val permission = businessClient.getPermission(userId, businessId).getOrElse { return Result.failure(it) }
        val business = businessClient.getBusinessById(businessId).getOrElse { return Result.failure(it) }
        return transactionManager.transaction<Unit> {
            permission.assert(ObjectPermission.OWNER)
            subscriptionSource.attachBusiness(
                BusinessSnapshot(
                    id = business.id,
                    name = business.name,
                    address = business.address,
                    timeZone = business.timeZone,
                    isEnabled = true,
                    schedule = business.schedule
                )
            )
            permissionsDataSource.initPermissions(userId, businessId, ObjectPermission.OWNER.int)
            settingsDataSource.create(AppointmentSettings(businessId, business.timeZone))
        }.onConstraintFailure {
            throw EnableAppointmentsForBusiness.Error.AlreadyEnabled()
        }
    }
}
