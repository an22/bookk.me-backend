package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.api.service.operation.CreateService.Error
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.onConstraintFailure
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class CreateServiceImpl(
    private val dataSource: ServiceDataSource,
    private val businessPermissionDataSource: BusinessPermissionDataSource,
    private val transactionManager: TransactionManager
) : CreateService {
    override suspend fun invoke(requestUserId: Uuid, service: Service): Result<Service> {
        if (service.name.isBlank()) return Result.failure(Error.ValidationError())
        return transactionManager.transaction {
            businessPermissionDataSource.getPermission(requestUserId, service.businessId).assert(ObjectPermission.EDIT)
            dataSource.createService(service)
        }.onConstraintFailure {
            throw Error.ServiceExist()
        }
    }
}