package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class CreateAppointmentRequestImpl(
    private val requestDataSource: AppointmentRequestDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val createAppointment: CreateAppointment,
    private val transactionManager: TransactionManager
) : CreateAppointmentRequest {

    override suspend fun invoke(
        userId: Uuid,
        request: AppointmentRequest
    ): Result<Unit> = transactionManager.transaction {
        val settings = settingsDataSource.getForUpdate(request.businessId) ?: throw Error.NotFound()
        permissionsDataSource.getPermissions(userId, request.businessId).assert(ObjectPermission.WRITE)
        if (settings.automaticApproval) {
            return@transaction createAppointment(userId, request)
                .map { Unit }
                .getOrThrow()
        }
        if (settings.isInWorkday(request.date)) throw CreateAppointmentRequest.Error.RequestForThisDateNotAllowed()
        if (settings.isInWorktime(request.date)) throw CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed()
        if (requestDataSource.hasOverlapsWith(request)) throw CreateAppointmentRequest.Error.RequestForThisTimeExists()
        requestDataSource.create(request)
    }
}